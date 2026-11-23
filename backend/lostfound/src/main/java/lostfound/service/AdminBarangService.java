package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.PenemuBarangRepository;
import lostfound.util.Fmt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Daftar barang untuk halaman admin.
 *
 * Dua konsep yang sengaja dipisah:
 *  - LAPORAN: data laporan di sistem (hilang/temuan), siapa pun yang melapor.
 *  - INVENTARIS POSKO: barang temuan yang SECARA FISIK sudah diterima admin di Posko
 *    (penemu_barang.waktu_diterima terisi). Laporan temuan yang belum diserahkan ke
 *    Posko muncul di daftar "Menunggu Penerimaan", bukan di inventaris.
 */
@Service
public class AdminBarangService {

    public record BarangRow(
            String kode,
            String jenis,        // HILANG / TEMUAN
            Long id,
            String nama,
            String kategori,
            String lokasi,
            String lokasiPenyimpanan,
            String tanggal,
            String dilaporkan,
            String diterima,     // waktu diterima di Posko ("-" bila belum)
            String status,
            String statusLabel,
            String pelapor,
            LocalDateTime dibuat,
            String alasan) {             // hanya untuk arsip: TIDAK DIKLAIM / TIDAK DITEMUKAN / PROSES TIDAK DILANJUTKAN

        public boolean hilang() {
            return "HILANG".equals(jenis);
        }

        public String jenisPath() {
            return hilang() ? "hilang" : "temuan";
        }
    }

    private final KehilanganBarangRepository kehilanganRepository;
    private final PenemuBarangRepository penemuRepository;
    private final LogAktivitasService logAktivitasService;
    private final PengaturanService pengaturanService;
    private final KlaimAtauTanggapanRepository klaimRepository;

    public AdminBarangService(KehilanganBarangRepository kehilanganRepository,
                              PenemuBarangRepository penemuRepository,
                              LogAktivitasService logAktivitasService,
                              PengaturanService pengaturanService,
                              KlaimAtauTanggapanRepository klaimRepository) {
        this.klaimRepository = klaimRepository;
        this.kehilanganRepository = kehilanganRepository;
        this.penemuRepository = penemuRepository;
        this.logAktivitasService = logAktivitasService;
        this.pengaturanService = pengaturanService;
    }

    // ---------- Inventaris Posko ----------

    /** Barang temuan yang sudah diterima di Posko. status: tersedia (Di Posko) / selesai (Diserahkan). */
    @Transactional(readOnly = true)
    public List<BarangRow> inventarisPosko(String q, int idKategori, String status) {
        String kunci = q == null ? "" : q.trim();
        List<BarangRow> hasil = new ArrayList<>();
        for (PenemuBarang p : penemuRepository.telusuriDiPosko(
                kunci, idKategori, BarangQueryService.statusPublik(status))) {
            hasil.add(barisTemuan(p, p.getStatus() == StatusBarang.TERSEDIA ? "Di Posko" : "Sudah Diserahkan"));
        }
        return hasil;
    }

    /** Laporan temuan yang belum diterima di Posko (belum jadi inventaris). */
    @Transactional(readOnly = true)
    public List<BarangRow> menungguPenerimaan(String q, int idKategori) {
        String kunci = q == null ? "" : q.trim();
        List<BarangRow> hasil = new ArrayList<>();
        for (PenemuBarang p : penemuRepository.telusuriBelumDiterima(
                kunci, idKategori, StatusBarang.TERSEDIA)) {
            hasil.add(barisTemuan(p, "Belum diterima"));
        }
        return hasil;
    }

    @Transactional(readOnly = true)
    public long hitungInventaris() {
        return penemuRepository.countByStatusAndDihapusFalseAndWaktuDiterimaIsNotNull(StatusBarang.TERSEDIA)
                + penemuRepository.countByStatusAndDihapusFalseAndWaktuDiterimaIsNotNull(StatusBarang.SELESAI);
    }

    @Transactional(readOnly = true)
    public long hitungMenungguPenerimaan() {
        return penemuRepository.countByStatusAndDihapusFalseAndWaktuDiterimaIsNull(StatusBarang.TERSEDIA);
    }

    /** Admin mencatat barang temuan sudah diterima fisik di Posko. */
    @Transactional
    public void terimaDiPosko(Long idPenemu, String lokasiPenyimpanan, Pengguna admin) {
        PenemuBarang p = penemuRepository.findDetail(idPenemu)
                .orElseThrow(() -> new IllegalArgumentException("Laporan temuan tidak ditemukan"));

        if (p.getStatus() != StatusBarang.TERSEDIA) {
            throw new IllegalArgumentException("Hanya laporan yang masih tersedia yang dapat diterima di Posko");
        }
        if (p.getWaktuDiterima() != null) {
            throw new IllegalArgumentException("Barang ini sudah tercatat diterima di Posko");
        }

        String lokasi = lokasiPenyimpanan == null ? "" : lokasiPenyimpanan.trim();
        if (lokasi.isEmpty()) {
            lokasi = pengaturanService.posko().lokasi();
        }
        if (lokasi.length() > 255) {
            throw new IllegalArgumentException("Lokasi penyimpanan maksimal 255 karakter");
        }

        p.setLokasiPenyimpanan(lokasi);
        p.setWaktuDiterima(LocalDateTime.now());
        penemuRepository.save(p);

        logAktivitasService.catatAktivitas(admin, null, p,
                "Menerima barang \"" + p.getNamaBarang() + "\" di " + lokasi);
    }

    // ---------- Arsip ----------

    /** Arsip = laporan berstatus DIARSIPKAN (hilang maupun temuan). */
    @Transactional(readOnly = true)
    public List<BarangRow> arsip(String q, int idKategori, String jenis) {
        String kunci = q == null ? "" : q.trim();
        String j = BarangQueryService.normalisasiJenis(jenis);
        Set<StatusBarang> arsip = EnumSet.of(StatusBarang.DIARSIPKAN);
        List<BarangRow> hasil = new ArrayList<>();

        if (j == null || BarangQueryService.HILANG.equals(j)) {
            for (KehilanganBarang k : kehilanganRepository.telusuri(kunci, idKategori, arsip)) {
                hasil.add(new BarangRow(
                        Fmt.kode("HLG", k.getIdKehilangan()), "HILANG", k.getIdKehilangan(),
                        k.getNamaBarang(), k.getKategori().getNamaKategori(),
                        k.getLokasiKejadian(), "-",
                        Fmt.tanggal(k.getTanggalKejadian()), Fmt.tanggal(k.getWaktuDibuat()), "-",
                        k.getStatus().name(), BarangQueryService.labelStatus(k.getStatus()),
                        k.getPengguna().getNamaLengkap(), k.getWaktuDibuat(),
                        ArsipBarangService.alasan(false,
                                klaimRepository.existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                                        k.getIdKehilangan(), StatusInteraksi.DISETUJUI))));
            }
        }
        if (j == null || BarangQueryService.TEMUAN.equals(j)) {
            for (PenemuBarang p : penemuRepository.telusuri(kunci, idKategori, arsip)) {
                hasil.add(barisTemuan(p, BarangQueryService.labelStatus(p.getStatus()),
                        ArsipBarangService.alasan(true,
                                klaimRepository.existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                                        p.getIdPenemu(), StatusInteraksi.DISETUJUI))));
            }
        }
        hasil.sort(Comparator.comparing(BarangRow::dibuat,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return hasil;
    }

    @Transactional(readOnly = true)
    public long hitungArsip() {
        return kehilanganRepository.countByStatusAndDihapusFalse(StatusBarang.DIARSIPKAN)
                + penemuRepository.countByStatusAndDihapusFalse(StatusBarang.DIARSIPKAN);
    }

    private BarangRow barisTemuan(PenemuBarang p, String labelStatus) {
        return barisTemuan(p, labelStatus, "-");
    }

    private BarangRow barisTemuan(PenemuBarang p, String labelStatus, String alasan) {
        boolean diterima = p.getWaktuDiterima() != null;
        return new BarangRow(
                Fmt.kode("TMN", p.getIdPenemu()), "TEMUAN", p.getIdPenemu(),
                p.getNamaBarang(), p.getKategori().getNamaKategori(),
                p.getLokasiDitemukan(),
                diterima && p.getLokasiPenyimpanan() != null ? p.getLokasiPenyimpanan() : "-",
                Fmt.tanggal(p.getTanggalDitemukan()), Fmt.tanggal(p.getWaktuDibuat()),
                diterima ? Fmt.tanggalWaktu(p.getWaktuDiterima()) : "-",
                p.getStatus().name(), labelStatus,
                p.getPengguna().getNamaLengkap(), p.getWaktuDibuat(), alasan);
    }
}
