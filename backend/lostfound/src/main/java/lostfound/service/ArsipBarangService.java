package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.PenemuBarang;
import lostfound.model.StatusBarang;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.PenemuBarangRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Arsip otomatis. Hanya laporan berstatus TERSEDIA yang bisa diarsipkan (SELESAI tidak pernah).
 *
 *  - Tanpa interaksi: diarsipkan bila 7 hari tanpa aktivitas (dihitung dari waktu_dibuat,
 *    yang juga di-reset saat admin memulihkan laporan, sehingga timer mulai ulang).
 *  - Ada interaksi MENUNGGU_VERIFIKASI: tidak pernah diarsipkan selama proses berjalan.
 *  - Ada interaksi DISETUJUI: diarsipkan bila 7 hari tanpa progres penyerahan
 *    (dihitung dari persetujuan, atau penerimaan di Posko bila lebih baru).
 *    Alasan: PROSES TIDAK DILANJUTKAN. Status interaksi TIDAK diubah.
 *  - Hanya interaksi DITOLAK: timer dihitung dari aktivitas terakhir (termasuk penolakan).
 *
 * Alasan arsip tidak disimpan; diturunkan saat ditampilkan (lihat alasan()).
 */
@Service
public class ArsipBarangService {

    private static final Logger log = LoggerFactory.getLogger(ArsipBarangService.class);

    public static final int BATAS_HARI = 7;
    public static final String TIDAK_DIKLAIM = "TIDAK DIKLAIM";
    public static final String TIDAK_DITEMUKAN = "TIDAK DITEMUKAN";
    public static final String PROSES_TIDAK_DILANJUTKAN = "PROSES TIDAK DILANJUTKAN";

    @Autowired
    private KehilanganBarangRepository kehilanganBarangRepository;

    @Autowired
    private PenemuBarangRepository penemuBarangRepository;

    @Autowired
    private KlaimAtauTanggapanRepository klaimRepository;

    /** Setiap jam tepat. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void arsipkanLaporanKedaluwarsa() {
        proses();
    }

    /** Sekali saat aplikasi mulai, agar laporan kedaluwarsa tidak menunggu jam berikutnya. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void arsipkanSaatStart() {
        proses();
    }

    private void proses() {
        LocalDateTime sekarang = LocalDateTime.now();
        int jumlah = 0;

        for (KehilanganBarang barang : kehilanganBarangRepository
                .findByStatusAndDihapusFalse(StatusBarang.TERSEDIA)) {
            if (perluDiarsipkan(sekarang, barang.getWaktuDibuat(), null,
                    klaimRepository.findByKehilanganBarang_IdKehilangan(barang.getIdKehilangan()))) {
                barang.setStatus(StatusBarang.DIARSIPKAN);
                kehilanganBarangRepository.save(barang);
                jumlah++;
            }
        }

        for (PenemuBarang barang : penemuBarangRepository
                .findByStatusAndDihapusFalse(StatusBarang.TERSEDIA)) {
            if (perluDiarsipkan(sekarang, barang.getWaktuDibuat(), barang.getWaktuDiterima(),
                    klaimRepository.findByPenemuBarang_IdPenemu(barang.getIdPenemu()))) {
                barang.setStatus(StatusBarang.DIARSIPKAN);
                penemuBarangRepository.save(barang);
                jumlah++;
            }
        }

        if (jumlah > 0) {
            log.info("[Arsip] {} laporan diarsipkan otomatis.", jumlah);
        }
    }

    /** Menentukan apakah satu laporan TERSEDIA harus diarsipkan, berdasarkan interaksinya. */
    static boolean perluDiarsipkan(LocalDateTime sekarang, LocalDateTime waktuDibuat,
                                   LocalDateTime waktuDiterima, List<KlaimAtauTanggapan> interaksi) {
        boolean menunggu = false;
        LocalDateTime disetujuiSejak = null;
        LocalDateTime aktivitasTerakhir = null;

        for (KlaimAtauTanggapan i : interaksi) {
            LocalDateTime waktu = i.getWaktuVerifikasi() != null ? i.getWaktuVerifikasi() : i.getWaktuDibuat();
            switch (i.getStatusInteraksi()) {
                case MENUNGGU_VERIFIKASI -> menunggu = true;
                case DISETUJUI -> disetujuiSejak = terbaru(disetujuiSejak, waktu);
                default -> aktivitasTerakhir = terbaru(aktivitasTerakhir, waktu);
            }
        }
        if (disetujuiSejak != null) {
            // Progres penyerahan = yang terbaru dari: persetujuan, penerimaan barang di Posko, dan
            // waktu_dibuat (di-reset saat admin memulihkan laporan, sehingga timer 7 hari mulai ulang
            // walau interaksi DISETUJUI lama masih tersimpan).
            disetujuiSejak = terbaru(terbaru(disetujuiSejak, waktuDiterima), waktuDibuat);
        }
        return perluDiarsipkan(sekarang, waktuDibuat, menunggu, disetujuiSejak, aktivitasTerakhir);
    }

    static boolean perluDiarsipkan(LocalDateTime sekarang, LocalDateTime waktuDibuat, boolean adaMenunggu,
                                   LocalDateTime disetujuiSejak, LocalDateTime aktivitasTerakhir) {
        if (adaMenunggu) {
            return false;
        }
        LocalDateTime batas = sekarang.minusDays(BATAS_HARI);
        if (disetujuiSejak != null) {
            return disetujuiSejak.isBefore(batas);
        }
        LocalDateTime acuan = terbaru(waktuDibuat, aktivitasTerakhir);
        return acuan != null && acuan.isBefore(batas);
    }

    private static LocalDateTime terbaru(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    // ---------- Alasan arsip (diturunkan, tidak disimpan) ----------

    public static String alasan(boolean temuan, boolean adaInteraksiDisetujui) {
        if (adaInteraksiDisetujui) {
            return PROSES_TIDAK_DILANJUTKAN;
        }
        return temuan ? TIDAK_DIKLAIM : TIDAK_DITEMUKAN;
    }

    public static String judul(String alasan) {
        return switch (alasan) {
            case TIDAK_DIKLAIM -> "Laporan Diarsipkan — Tidak Diklaim";
            case TIDAK_DITEMUKAN -> "Laporan Diarsipkan — Tidak Ditemukan";
            default -> "Laporan Diarsipkan — Proses Tidak Dilanjutkan";
        };
    }

    public static String pesanPemilik(String alasan) {
        String sebab = switch (alasan) {
            case TIDAK_DIKLAIM -> "tidak ada klaim selama 7 hari";
            case TIDAK_DITEMUKAN -> "tidak ada tanggapan atau penemuan selama 7 hari";
            default -> "proses penyerahan tidak dilanjutkan selama 7 hari setelah disetujui";
        };
        return "Laporan ini telah diarsipkan karena " + sebab
                + ". Jika kamu ingin laporan ini ditampilkan kembali, silakan hubungi admin FindUp!.";
    }
}
