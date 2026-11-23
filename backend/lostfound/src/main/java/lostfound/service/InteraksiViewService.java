package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.util.Fmt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data klaim/tanggapan untuk halaman web (mahasiswa & admin).
 * Perubahan status tetap dikerjakan KlaimAtauTanggapanService.
 */
@Service
@Transactional(readOnly = true)
public class InteraksiViewService {

    public record KlaimRow(
            Long id,
            String kode,
            String namaBarang,
            String jenisBarang,        // HILANG / TEMUAN
            String jenisInteraksi,     // teks untuk tabel
            String kodeBarang,
            Long idBarang,
            String diajukan,
            String diajukanSingkat,
            String status,             // enum name
            String statusLabel,
            String statusCode,         // wait / ok / done / no
            String tenggat,
            String alasanPenolakan,
            String pemohon,
            String emailPemohon,
            String teleponPemohon,
            String diverifikasiOleh,
            String waktuVerifikasi,
            String waktuSelesai,
            boolean adaFotoKtm,
            boolean adaFotoBukti,
            boolean barangDiarsipkan) {   // laporan terkait sudah diarsipkan

        public boolean hilang() {
            return "HILANG".equals(jenisBarang);
        }

        public boolean menunggu() {
            return "wait".equals(statusCode);
        }

        public boolean disetujui() {
            return "ok".equals(statusCode);
        }

        public boolean selesai() {
            return "done".equals(statusCode);
        }

        public boolean ditolak() {
            return "no".equals(statusCode);
        }
    }

    public record KlaimDetail(
            KlaimRow row,
            String catatanBukti,
            String spesifikasiRahasia,
            String deskripsiBarang,
            String lokasiBarang,
            String lokasiPenyimpanan,
            String urlGambarBarang,
            String kategori,
            String pelaporBarang,
            String statusBarang,
            boolean barangTersedia,
            boolean perluPenerimaan) {   // true bila barang temuan belum diterima di Posko
    }

    private final KlaimAtauTanggapanRepository klaimRepository;

    public InteraksiViewService(KlaimAtauTanggapanRepository klaimRepository) {
        this.klaimRepository = klaimRepository;
    }

    public static StatusInteraksi dariKode(String kode) {
        if (kode == null) {
            return null;
        }
        return switch (kode.trim().toLowerCase()) {
            case "wait" -> StatusInteraksi.MENUNGGU_VERIFIKASI;
            case "ok" -> StatusInteraksi.DISETUJUI;
            case "done" -> StatusInteraksi.SELESAI;
            case "no" -> StatusInteraksi.DITOLAK;
            default -> null;
        };
    }

    public static String kodeStatus(StatusInteraksi s) {
        return switch (s) {
            case MENUNGGU_VERIFIKASI -> "wait";
            case DISETUJUI -> "ok";
            case SELESAI -> "done";
            case DITOLAK -> "no";
        };
    }

    public static String labelStatus(StatusInteraksi s) {
        return switch (s) {
            case MENUNGGU_VERIFIKASI -> "Menunggu Verifikasi";
            case DISETUJUI -> "Disetujui";
            case SELESAI -> "Selesai";
            case DITOLAK -> "Ditolak";
        };
    }

    // ---------- mahasiswa ----------

    public List<KlaimRow> milikSaya(Integer idPengguna, String statusKode) {
        StatusInteraksi filter = dariKode(statusKode);
        List<KlaimRow> hasil = new ArrayList<>();
        for (KlaimAtauTanggapan i : klaimRepository.milikPengguna(idPengguna)) {
            if (filter == null || i.getStatusInteraksi() == filter) {
                hasil.add(baris(i));
            }
        }
        return hasil;
    }

    public long hitungMilikSaya(Integer idPengguna) {
        return klaimRepository.milikPengguna(idPengguna).size();
    }

    public Optional<KlaimDetail> detailPemohon(Long id, Integer idPengguna) {
        return klaimRepository.findDetail(id)
                .filter(i -> i.getPenggunaMerespons().getIdPengguna().equals(idPengguna))
                .map(this::detail);
    }

    // ---------- admin ----------

    public List<KlaimRow> antrean(String statusKode, String q) {
        StatusInteraksi filter = dariKode(statusKode);
        String kunci = q == null ? "" : q.trim().toLowerCase();
        List<KlaimRow> hasil = new ArrayList<>();
        for (KlaimAtauTanggapan i : klaimRepository.semuaDenganDetail()) {
            if (filter != null && i.getStatusInteraksi() != filter) {
                continue;
            }
            KlaimRow r = baris(i);
            if (kunci.isEmpty()
                    || r.namaBarang().toLowerCase().contains(kunci)
                    || r.pemohon().toLowerCase().contains(kunci)) {
                hasil.add(r);
            }
        }
        return hasil;
    }

    public Optional<KlaimDetail> detailAdmin(Long id) {
        return klaimRepository.findDetail(id).map(this::detail);
    }

    /** Riwayat penyerahan = interaksi berstatus SELESAI. jenis: hilang / temuan / kosong. */
    public List<KlaimRow> riwayat(String q, String jenis) {
        String j = BarangQueryService.normalisasiJenis(jenis);
        String kunci = q == null ? "" : q.trim().toLowerCase();
        List<KlaimRow> hasil = new ArrayList<>();
        for (KlaimAtauTanggapan i : klaimRepository.semuaDenganDetail()) {
            if (i.getStatusInteraksi() != StatusInteraksi.SELESAI) {
                continue;
            }
            KlaimRow r = baris(i);
            if (j != null && !j.equals(r.jenisBarang())) {
                continue;
            }
            if (kunci.isEmpty()
                    || r.namaBarang().toLowerCase().contains(kunci)
                    || r.pemohon().toLowerCase().contains(kunci)) {
                hasil.add(r);
            }
        }
        // Terbaru diserahkan lebih dulu
        hasil.sort((a, b) -> Long.compare(b.id(), a.id()));
        return hasil;
    }

    public long hitungStatus(StatusInteraksi status) {
        return klaimRepository.countByStatusInteraksi(status);
    }

    public long hitungSemua() {
        return klaimRepository.count();
    }

    // ---------- akses file KTM ----------

    /** Path relatif foto KTM bila pengguna berhak melihatnya (admin atau pemohon). */
    public Optional<String> pathKtmUntuk(Long id, Pengguna pengguna) {
        return klaimRepository.findById(id)
                .filter(i -> pengguna.getPeran() == PeranPengguna.ADMIN
                        || i.getPenggunaMerespons().getIdPengguna().equals(pengguna.getIdPengguna()))
                .map(KlaimAtauTanggapan::getFotoKtm)
                .filter(p -> p != null && !p.isBlank());
    }

    /** Path relatif foto bukti pendukung bila pengguna berhak (admin atau pemohon). */
    public Optional<String> pathBuktiUntuk(Long id, Pengguna pengguna) {
        return klaimRepository.findById(id)
                .filter(i -> pengguna.getPeran() == PeranPengguna.ADMIN
                        || i.getPenggunaMerespons().getIdPengguna().equals(pengguna.getIdPengguna()))
                .map(KlaimAtauTanggapan::getFotoBukti)
                .filter(p -> p != null && !p.isBlank());
    }

    // ---------- mapping ----------

    private KlaimRow baris(KlaimAtauTanggapan i) {
        boolean hilang = i.getKehilanganBarang() != null;
        Pengguna pemohon = i.getPenggunaMerespons();

        String nama;
        String kodeBarang;
        Long idBarang;
        if (hilang) {
            KehilanganBarang k = i.getKehilanganBarang();
            nama = k.getNamaBarang();
            kodeBarang = Fmt.kode("HLG", k.getIdKehilangan());
            idBarang = k.getIdKehilangan();
        } else {
            PenemuBarang p = i.getPenemuBarang();
            nama = p.getNamaBarang();
            kodeBarang = Fmt.kode("TMN", p.getIdPenemu());
            idBarang = p.getIdPenemu();
        }

        return new KlaimRow(
                i.getIdInteraksi(),
                Fmt.kode("KLM", i.getIdInteraksi()),
                nama,
                hilang ? "HILANG" : "TEMUAN",
                hilang ? "Tanggapan laporan kehilangan" : "Klaim barang temuan",
                kodeBarang,
                idBarang,
                Fmt.tanggalWaktu(i.getWaktuDibuat()),
                Fmt.tanggalSingkat(i.getWaktuDibuat()),
                i.getStatusInteraksi().name(),
                labelStatus(i.getStatusInteraksi()),
                kodeStatus(i.getStatusInteraksi()),
                i.getTenggatTanggapan() == null ? "-" : Fmt.tanggal(i.getTenggatTanggapan()),
                i.getAlasanPenolakan(),
                pemohon.getNamaLengkap(),
                pemohon.getEmail(),
                pemohon.getNomorTelepon(),
                i.getAdminVerifikator() == null ? "-" : i.getAdminVerifikator().getNamaLengkap(),
                i.getWaktuVerifikasi() == null ? "-" : Fmt.tanggalWaktu(i.getWaktuVerifikasi()),
                i.getWaktuSelesai() == null ? "-" : Fmt.tanggalWaktu(i.getWaktuSelesai()),
                i.getFotoKtm() != null && !i.getFotoKtm().isBlank(),
                i.getFotoBukti() != null && !i.getFotoBukti().isBlank(),
                hilang
                        ? i.getKehilanganBarang().getStatus() == StatusBarang.DIARSIPKAN
                        : i.getPenemuBarang().getStatus() == StatusBarang.DIARSIPKAN);
    }

    private KlaimDetail detail(KlaimAtauTanggapan i) {
        KlaimRow row = baris(i);
        if (i.getKehilanganBarang() != null) {
            KehilanganBarang k = i.getKehilanganBarang();
            return new KlaimDetail(row, i.getCatatanBukti(),
                    k.getSpesifikasiRahasia(), k.getDeskripsiPublik(),
                    k.getLokasiKejadian(), null, Fmt.urlGambarAman(k.getUrlGambar()),
                    k.getKategori().getNamaKategori(),
                    k.getPengguna().getNamaLengkap(),
                    k.getStatus().name(), k.getStatus() == StatusBarang.TERSEDIA, false);
        }
        PenemuBarang p = i.getPenemuBarang();
        return new KlaimDetail(row, i.getCatatanBukti(),
                null, p.getDeskripsi(),
                p.getLokasiDitemukan(),
                p.getWaktuDiterima() != null ? p.getLokasiPenyimpanan() : null,
                Fmt.urlGambarAman(p.getUrlGambar()),
                p.getKategori().getNamaKategori(),
                p.getPengguna().getNamaLengkap(),
                p.getStatus().name(), p.getStatus() == StatusBarang.TERSEDIA,
                p.getWaktuDiterima() == null);
    }
}
