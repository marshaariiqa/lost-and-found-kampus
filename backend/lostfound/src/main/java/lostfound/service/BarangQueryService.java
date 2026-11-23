package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.PenemuBarang;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.model.Pengguna;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.PenemuBarangRepository;
import lostfound.util.Fmt;
import lostfound.view.BarangCard;
import lostfound.view.BarangDetailView;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Query barang untuk halaman web. Seluruh hasil berupa view record
 * (bukan entity) sehingga data rahasia tidak ikut terkirim ke template.
 */
@Service
@Transactional(readOnly = true)
public class BarangQueryService {

    public static final String HILANG = "HILANG";
    public static final String TEMUAN = "TEMUAN";

    private final KehilanganBarangRepository kehilanganRepository;
    private final PenemuBarangRepository penemuRepository;
    private final KlaimAtauTanggapanRepository klaimRepository;

    public BarangQueryService(KehilanganBarangRepository kehilanganRepository,
                              PenemuBarangRepository penemuRepository,
                              KlaimAtauTanggapanRepository klaimRepository) {
        this.kehilanganRepository = kehilanganRepository;
        this.penemuRepository = penemuRepository;
        this.klaimRepository = klaimRepository;
    }

    /** "hilang" / "temuan" -> HILANG / TEMUAN; selain itu null (semua jenis). */
    public static String normalisasiJenis(String jenis) {
        if (jenis == null) {
            return null;
        }
        return switch (jenis.trim().toLowerCase()) {
            case "hilang", "lost" -> HILANG;
            case "temuan", "found", "ditemukan" -> TEMUAN;
            default -> null;
        };
    }

    /** "tersedia" / "selesai" -> himpunan status; kosong -> TERSEDIA + SELESAI. */
    public static Set<StatusBarang> statusPublik(String status) {
        if (status != null) {
            switch (status.trim().toLowerCase()) {
                case "tersedia":
                    return EnumSet.of(StatusBarang.TERSEDIA);
                case "selesai":
                    return EnumSet.of(StatusBarang.SELESAI);
                default:
                    break;
            }
        }
        return EnumSet.of(StatusBarang.TERSEDIA, StatusBarang.SELESAI);
    }

    /** Daftar gabungan hilang + temuan, terbaru lebih dulu. */
    public List<BarangCard> telusuri(String jenis, String q, int idKategori,
                                     Set<StatusBarang> statuses, Integer idSaya) {
        String kunci = q == null ? "" : q.trim();
        String j = normalisasiJenis(jenis);
        List<BarangCard> hasil = new ArrayList<>();

        if (j == null || HILANG.equals(j)) {
            for (KehilanganBarang k : kehilanganRepository.telusuri(kunci, idKategori, statuses)) {
                hasil.add(kartu(k, idSaya));
            }
        }
        if (j == null || TEMUAN.equals(j)) {
            for (PenemuBarang p : penemuRepository.telusuri(kunci, idKategori, statuses)) {
                hasil.add(kartu(p, idSaya));
            }
        }
        hasil.sort(Comparator.comparing(BarangCard::dibuat,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return hasil;
    }

    public List<BarangCard> terbaru(int jumlah, Integer idSaya) {
        List<BarangCard> semua = telusuri(null, "", 0,
                EnumSet.of(StatusBarang.TERSEDIA), idSaya);
        return semua.size() > jumlah ? new ArrayList<>(semua.subList(0, jumlah)) : semua;
    }

    /** Semua laporan milik satu pengguna (hilang + temuan, semua status). */
    public List<BarangCard> milikSaya(Integer idPengguna) {
        List<BarangCard> hasil = new ArrayList<>();
        for (KehilanganBarang k : kehilanganRepository.milikPengguna(idPengguna)) {
            BarangCard kartu = kartu(k, idPengguna);
            hasil.add(k.getStatus() == StatusBarang.TERSEDIA
                    ? perkayaAlur(kartu,
                        klaimRepository.countByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                                k.getIdKehilangan(), StatusInteraksi.MENUNGGU_VERIFIKASI),
                        klaimRepository.countByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                                k.getIdKehilangan(), StatusInteraksi.DISETUJUI),
                        klaimRepository.countByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                                k.getIdKehilangan(), StatusInteraksi.DITOLAK))
                    : kartu);
        }
        for (PenemuBarang p : penemuRepository.milikPengguna(idPengguna)) {
            BarangCard kartu = kartu(p, idPengguna);
            hasil.add(p.getStatus() == StatusBarang.TERSEDIA
                    ? perkayaAlur(kartu,
                        klaimRepository.countByPenemuBarang_IdPenemuAndStatusInteraksi(
                                p.getIdPenemu(), StatusInteraksi.MENUNGGU_VERIFIKASI),
                        klaimRepository.countByPenemuBarang_IdPenemuAndStatusInteraksi(
                                p.getIdPenemu(), StatusInteraksi.DISETUJUI),
                        klaimRepository.countByPenemuBarang_IdPenemuAndStatusInteraksi(
                                p.getIdPenemu(), StatusInteraksi.DITOLAK))
                    : kartu);
        }
        hasil.sort(Comparator.comparing(BarangCard::dibuat,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return hasil;
    }

    public Optional<BarangDetailView> detail(String jenis, Long id, Pengguna saya) {
        String j = normalisasiJenis(jenis);
        if (j == null || id == null) {
            return Optional.empty();
        }
        // detailHilang/detailTemuan mengembalikan null bila laporan tidak boleh dilihat pengguna ini
        return HILANG.equals(j)
                ? kehilanganRepository.findDetail(id).map(k -> detailHilang(k, saya))
                : penemuRepository.findDetail(id).map(p -> detailTemuan(p, saya));
    }

    /** Alasan arsip diturunkan: ada pengajuan disetujui -> proses tidak dilanjutkan. */
    private String alasanArsip(boolean temuan, Long idLaporan) {
        boolean disetujui = temuan
                ? klaimRepository.existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                        idLaporan, StatusInteraksi.DISETUJUI)
                : klaimRepository.existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                        idLaporan, StatusInteraksi.DISETUJUI);
        return ArsipBarangService.alasan(temuan, disetujui);
    }

    private String judulArsip(StatusBarang status, boolean temuan, Long idLaporan) {
        return status == StatusBarang.DIARSIPKAN
                ? ArsipBarangService.judul(alasanArsip(temuan, idLaporan)) : null;
    }

    private String pesanArsip(StatusBarang status, boolean temuan, Long idLaporan, boolean pemilik) {
        if (status != StatusBarang.DIARSIPKAN) {
            return null;
        }
        return pemilik
                ? ArsipBarangService.pesanPemilik(alasanArsip(temuan, idLaporan))
                : "Laporan ini telah diarsipkan dan tidak lagi aktif.";
    }

    // ---------- mapping ----------

    private BarangCard kartu(KehilanganBarang k, Integer idSaya) {
        return new BarangCard(HILANG, k.getIdKehilangan(),
                Fmt.kode("HLG", k.getIdKehilangan()),
                k.getNamaBarang(),
                k.getKategori().getNamaKategori(),
                k.getLokasiKejadian(),
                Fmt.tanggal(k.getTanggalKejadian()),
                k.getStatus().name(),
                labelStatus(k.getStatus()),
                Fmt.urlGambarAman(k.getUrlGambar()),
                idSaya != null && idSaya.equals(k.getPengguna().getIdPengguna()),
                Fmt.namaSingkat(k.getPengguna().getNamaLengkap()),
                k.getWaktuDibuat(),
                alurDasar(k.getStatus()), labelStatus(k.getStatus()), "");
    }

    private BarangCard kartu(PenemuBarang p, Integer idSaya) {
        return new BarangCard(TEMUAN, p.getIdPenemu(),
                Fmt.kode("TMN", p.getIdPenemu()),
                p.getNamaBarang(),
                p.getKategori().getNamaKategori(),
                p.getLokasiDitemukan(),
                Fmt.tanggal(p.getTanggalDitemukan()),
                p.getStatus().name(),
                labelStatus(p.getStatus()),
                Fmt.urlGambarAman(p.getUrlGambar()),
                idSaya != null && idSaya.equals(p.getPengguna().getIdPengguna()),
                Fmt.namaSingkat(p.getPengguna().getNamaLengkap()),
                p.getWaktuDibuat(),
                alurDasar(p.getStatus()), labelStatus(p.getStatus()), "");
    }

    private BarangDetailView detailHilang(KehilanganBarang k, Pengguna saya) {
        boolean milikSaya = saya != null
                && saya.getIdPengguna().equals(k.getPengguna().getIdPengguna());
        boolean tersedia = k.getStatus() == StatusBarang.TERSEDIA;

        Long idInteraksi = null;
        String statusInteraksi = null;
        if (saya != null && !milikSaya) {
            List<Long> ids = klaimRepository.idInteraksiPenggunaPadaKehilangan(
                    k.getIdKehilangan(), saya.getIdPengguna());
            if (!ids.isEmpty()) {
                idInteraksi = ids.get(0);
                statusInteraksi = klaimRepository.findById(idInteraksi)
                        .map(i -> i.getStatusInteraksi().name()).orElse(null);
            }
        }

        // Laporan SELESAI/DIARSIPKAN hanya boleh dibuka pemiliknya atau pihak yang pernah mengajukan
        if (!tersedia && !milikSaya && idInteraksi == null) {
            return null;
        }

        long menunggu = milikSaya
                ? klaimRepository.countByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                        k.getIdKehilangan(), StatusInteraksi.MENUNGGU_VERIFIKASI)
                : 0;

        return new BarangDetailView(HILANG, k.getIdKehilangan(),
                Fmt.kode("HLG", k.getIdKehilangan()),
                k.getNamaBarang(), k.getKategori().getNamaKategori(),
                k.getDeskripsiPublik(), k.getLokasiKejadian(),
                Fmt.tanggal(k.getTanggalKejadian()),
                k.getStatus().name(), labelStatus(k.getStatus()),
                Fmt.urlGambarAman(k.getUrlGambar()),
                Fmt.namaSingkat(k.getPengguna().getNamaLengkap()),
                null,
                milikSaya ? k.getSpesifikasiRahasia() : null,
                milikSaya,
                tersedia && !milikSaya && saya != null && !masihAktif(statusInteraksi),
                idInteraksi, statusInteraksi, menunggu,
                false,
                judulArsip(k.getStatus(), false, k.getIdKehilangan()),
                pesanArsip(k.getStatus(), false, k.getIdKehilangan(), milikSaya));
    }

    private BarangDetailView detailTemuan(PenemuBarang p, Pengguna saya) {
        boolean milikSaya = saya != null
                && saya.getIdPengguna().equals(p.getPengguna().getIdPengguna());
        boolean tersedia = p.getStatus() == StatusBarang.TERSEDIA;

        Long idInteraksi = null;
        String statusInteraksi = null;
        if (saya != null && !milikSaya) {
            List<Long> ids = klaimRepository.idInteraksiPenggunaPadaTemuan(
                    p.getIdPenemu(), saya.getIdPengguna());
            if (!ids.isEmpty()) {
                idInteraksi = ids.get(0);
                statusInteraksi = klaimRepository.findById(idInteraksi)
                        .map(i -> i.getStatusInteraksi().name()).orElse(null);
            }
        }

        if (!tersedia && !milikSaya && idInteraksi == null) {
            return null;
        }

        long menunggu = milikSaya
                ? klaimRepository.countByPenemuBarang_IdPenemuAndStatusInteraksi(
                        p.getIdPenemu(), StatusInteraksi.MENUNGGU_VERIFIKASI)
                : 0;

        return new BarangDetailView(TEMUAN, p.getIdPenemu(),
                Fmt.kode("TMN", p.getIdPenemu()),
                p.getNamaBarang(), p.getKategori().getNamaKategori(),
                p.getDeskripsi(), p.getLokasiDitemukan(),
                Fmt.tanggal(p.getTanggalDitemukan()),
                p.getStatus().name(), labelStatus(p.getStatus()),
                Fmt.urlGambarAman(p.getUrlGambar()),
                Fmt.namaSingkat(p.getPengguna().getNamaLengkap()),
                p.getWaktuDiterima() != null ? p.getLokasiPenyimpanan() : null,
                null,
                milikSaya,
                tersedia && !milikSaya && saya != null && !masihAktif(statusInteraksi),
                idInteraksi, statusInteraksi, menunggu,
                p.getWaktuDiterima() != null,
                judulArsip(p.getStatus(), true, p.getIdPenemu()),
                pesanArsip(p.getStatus(), true, p.getIdPenemu(), milikSaya));
    }

    /** Pengajuan dianggap masih berjalan bila menunggu verifikasi atau disetujui. */
    private static boolean masihAktif(String statusInteraksi) {
        return "MENUNGGU_VERIFIKASI".equals(statusInteraksi) || "DISETUJUI".equals(statusInteraksi);
    }

    private static String alurDasar(StatusBarang status) {
        return switch (status) {
            case TERSEDIA -> "avail";
            case SELESAI -> "done";
            case DIARSIPKAN -> "arch";
        };
    }

    /**
     * Status tampilan untuk pemilik laporan, diturunkan dari laporan + pengajuan padanya:
     * ada yang disetujui -> Disetujui; ada yang menunggu -> Menunggu Verifikasi;
     * hanya ada yang ditolak -> Ditolak; belum ada pengajuan -> Tersedia.
     */
    private static BarangCard perkayaAlur(BarangCard kartu, long menunggu, long disetujui, long ditolak) {
        if (disetujui > 0) {
            return kartu.conAlur("ok", "Disetujui", "Pengajuan disetujui, tinggal diserahkan di posko");
        }
        if (menunggu > 0) {
            return kartu.conAlur("wait", "Menunggu Verifikasi",
                    menunggu + " pengajuan sedang diperiksa admin");
        }
        if (ditolak > 0) {
            return kartu.conAlur("no", "Ditolak",
                    "Pengajuan terakhir ditolak admin; laporan masih tersedia");
        }
        return kartu.conAlur("avail", "Tersedia", "Belum ada pengajuan");
    }

    /** Data laporan untuk form edit (hanya diisi bila pemilik; status dicek di service saat disimpan). */
    public record BarangEdit(String jenis, Long id, String nama, Integer idKategori, String deskripsi,
                             String ciri, String lokasi, String tanggal, String urlGambar) {
    }

    public Optional<BarangEdit> dataEdit(String jenis, Long id, Pengguna saya) {
        String j = normalisasiJenis(jenis);
        if (j == null || id == null || saya == null) {
            return Optional.empty();
        }
        if (HILANG.equals(j)) {
            return kehilanganRepository.findDetail(id)
                    .filter(k -> k.getPengguna().getIdPengguna().equals(saya.getIdPengguna()))
                    .map(k -> new BarangEdit(HILANG, k.getIdKehilangan(), k.getNamaBarang(),
                            k.getKategori().getIdKategori(), k.getDeskripsiPublik(),
                            k.getSpesifikasiRahasia(), k.getLokasiKejadian(),
                            k.getTanggalKejadian() == null ? "" : k.getTanggalKejadian().toLocalDate().toString(),
                            k.getUrlGambar()));
        }
        return penemuRepository.findDetail(id)
                .filter(p -> p.getPengguna().getIdPengguna().equals(saya.getIdPengguna()))
                .map(p -> new BarangEdit(TEMUAN, p.getIdPenemu(), p.getNamaBarang(),
                        p.getKategori().getIdKategori(), p.getDeskripsi(),
                        null, p.getLokasiDitemukan(),
                        p.getTanggalDitemukan() == null ? "" : p.getTanggalDitemukan().toLocalDate().toString(),
                        p.getUrlGambar()));
    }

    public static String labelStatus(StatusBarang status) {
        return switch (status) {
            case TERSEDIA -> "Tersedia";
            case SELESAI -> "Selesai";
            case DIARSIPKAN -> "Diarsipkan";
        };
    }
}
