package lostfound.service;

import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.PenemuBarangRepository;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.KehilanganBarang;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.JenisNotifikasi;
import lostfound.model.StatusInteraksi;
import lostfound.model.StatusBarang;
import lostfound.model.PeranPengguna;

import lostfound.dto.KlaimAtauTanggapanResponseDTO;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class KlaimAtauTanggapanService {

    @Autowired
    private KlaimAtauTanggapanRepository klaimRepository;

    @Autowired
    private KehilanganBarangRepository kehilanganRepository;

    @Autowired
    private PenemuBarangRepository penemuRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private KlaimAtauTanggapanMapper klaimMapper;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private LogAktivitasService logAktivitasService;

    @Autowired
    private PengaturanService pengaturanService;

    private static final Set<StatusInteraksi> STATUS_AKTIF =
            Set.of(StatusInteraksi.MENUNGGU_VERIFIKASI, StatusInteraksi.DISETUJUI);

    @Transactional
    public KlaimAtauTanggapan ajukanTanggapanKehilangan(
            Long idKehilangan,
            String catatanBukti,
            String fotoKtm,
            Authentication authentication) {

        // Ambil mahasiswa yang sedang login
        Pengguna mahasiswa = ambilMahasiswaLogin(authentication);

        // Cari laporan kehilangan
        KehilanganBarang kehilangan = kehilanganRepository.findByIdKehilanganAndDihapusFalse(idKehilangan).orElseThrow(() ->
            new RuntimeException("Laporan kehilangan tidak ditemukan"));

        // Pastikan bukan laporan milik sendiri
        validasiBukanLaporanSendiri(
            kehilangan.getPengguna(),
            mahasiswa,
            "Tidak dapat menanggapi laporan milik sendiri"
        );

        // Pastikan barang masih tersedia
        validasiBarangTersedia(
            kehilangan.getStatus(),
            "Laporan ini sudah selesai"
        );

        // Validasi catatan bukti dan foto KTM (wajib)
        validasiCatatanBukti(catatanBukti);
        validasiFotoKtm(fotoKtm);

        // Cegah pengajuan ganda yang masih diproses pada laporan yang sama
        if (klaimRepository
                .existsByKehilanganBarang_IdKehilanganAndPenggunaMerespons_IdPenggunaAndStatusInteraksiIn(
                        idKehilangan, mahasiswa.getIdPengguna(), STATUS_AKTIF)) {
            throw new RuntimeException("Kamu sudah mengajukan tanggapan pada laporan ini");
        }

        // Buat interaksi baru
        KlaimAtauTanggapan interaksi = new KlaimAtauTanggapan();

        interaksi.setTenggatTanggapan(
                LocalDateTime.now().plusDays(pengaturanService.tenggatHari()));
        interaksi.setKehilanganBarang(kehilangan);
        interaksi.setPenemuBarang(null);
        interaksi.setPenggunaMerespons(mahasiswa);
        interaksi.setCatatanBukti(catatanBukti);
        interaksi.setFotoKtm(fotoKtm);
        interaksi.setStatusInteraksi(StatusInteraksi.MENUNGGU_VERIFIKASI);

        KlaimAtauTanggapan interaksiTersimpan =
        klaimRepository.save(interaksi);

        // Kirim notifikasi kepada pemilik laporan kehilangan
        notifikasiService.kirimNotifikasiInteraksiBaru(
            kehilangan.getPengguna(),
            interaksiTersimpan,
            kehilangan,
            null
        );

        return interaksiTersimpan;
    }

    @Transactional
    public KlaimAtauTanggapan ajukanKlaimTemuan(
            Long idPenemu,
            String catatanBukti,
            String fotoKtm,
            Authentication authentication) {

        // Ambil mahasiswa yang sedang login
        Pengguna mahasiswa = ambilMahasiswaLogin(authentication);

        // Cari laporan barang temuan
        PenemuBarang penemu = penemuRepository.findByIdPenemuAndDihapusFalse(idPenemu).orElseThrow(() ->
            new RuntimeException("Laporan barang temuan tidak ditemukan"));

        // Pastikan bukan laporan milik sendiri
        validasiBukanLaporanSendiri(
            penemu.getPengguna(),
            mahasiswa,
            "Tidak dapat mengajukan klaim pada laporan sendiri"
        );

        // Pastikan barang masih tersedia
        validasiBarangTersedia(
            penemu.getStatus(),
            "Barang ini sudah selesai diproses"
        );

        // Validasi catatan bukti dan foto KTM (wajib)
        validasiCatatanBukti(catatanBukti);
        validasiFotoKtm(fotoKtm);

        // Cegah pengajuan ganda yang masih diproses pada laporan yang sama
        if (klaimRepository
                .existsByPenemuBarang_IdPenemuAndPenggunaMerespons_IdPenggunaAndStatusInteraksiIn(
                        idPenemu, mahasiswa.getIdPengguna(), STATUS_AKTIF)) {
            throw new RuntimeException("Kamu sudah mengajukan klaim pada laporan ini");
        }

        // Buat interaksi baru
        KlaimAtauTanggapan interaksi = new KlaimAtauTanggapan();

        interaksi.setTenggatTanggapan(
                LocalDateTime.now().plusDays(pengaturanService.tenggatHari()));
        interaksi.setKehilanganBarang(null);
        interaksi.setPenemuBarang(penemu);
        interaksi.setPenggunaMerespons(mahasiswa);
        interaksi.setCatatanBukti(catatanBukti);
        interaksi.setFotoKtm(fotoKtm);
        interaksi.setStatusInteraksi(StatusInteraksi.MENUNGGU_VERIFIKASI);

        KlaimAtauTanggapan interaksiTersimpan =
        klaimRepository.save(interaksi);

        // Kirim notifikasi kepada pelapor barang temuan
        notifikasiService.kirimNotifikasiInteraksiBaru(
            penemu.getPengguna(),
            interaksiTersimpan,
            null,
            penemu
        );

        return interaksiTersimpan;
    }

    private void validasiCatatanBukti(String catatanBukti) {
        if (catatanBukti == null || catatanBukti.isBlank()) {
            throw new RuntimeException("Catatan bukti wajib diisi");
        }
    }

    // Foto KTM wajib. Validasi final ada di backend, tidak hanya di form HTML.
    private void validasiFotoKtm(String fotoKtm) {
        if (fotoKtm == null || fotoKtm.isBlank()) {
            throw new RuntimeException("Foto KTM wajib diunggah");
        }
    }

    private Pengguna ambilMahasiswaLogin(Authentication authentication) {
        Pengguna mahasiswa = penggunaRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        if (mahasiswa.getPeran() != PeranPengguna.MAHASISWA) {
            throw new RuntimeException("Hanya mahasiswa yang dapat mengajukan interaksi");
        }

        return mahasiswa;
    }

    private void validasiBukanLaporanSendiri(
            Pengguna pemilikLaporan,
            Pengguna mahasiswa,
            String pesanError) {

        if (pemilikLaporan.getIdPengguna().equals(mahasiswa.getIdPengguna())) {
            throw new RuntimeException(pesanError);
        }
    }

    private void validasiBarangTersedia(
            StatusBarang status,
            String pesanError) {

        if (status != StatusBarang.TERSEDIA) {
            throw new RuntimeException(pesanError);
        }
    }

    private Pengguna ambilAdminLogin(
        Authentication authentication,
        String pesanError) {

            Pengguna admin = penggunaRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

            if (admin.getPeran() != PeranPengguna.ADMIN) {
                throw new RuntimeException(pesanError);
            }

            return admin;
        }

        private KlaimAtauTanggapan ambilInteraksi(Long idInteraksi) {
            return klaimRepository.findById(idInteraksi)
                    .orElseThrow(() -> new RuntimeException("Interaksi tidak ditemukan"));
        }

        private void validasiStatusInteraksi(
                KlaimAtauTanggapan interaksi,
                StatusInteraksi status,
                String pesanError) {

            if (interaksi.getStatusInteraksi() != status) {
                throw new RuntimeException(pesanError);
            }
        }

    @Transactional
    public KlaimAtauTanggapan setujuiInteraksi(Long idInteraksi, Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = ambilAdminLogin(
            authentication,
            "Hanya admin yang dapat menyetujui interaksi"
        );

        // Cari interaksi
        KlaimAtauTanggapan interaksi = ambilInteraksi(idInteraksi);

        // Pastikan interaksi masih menunggu verifikasi
        validasiStatusInteraksi(
            interaksi,
            StatusInteraksi.MENUNGGU_VERIFIKASI,
            "Interaksi sudah pernah diverifikasi"
        );

        // Pastikan belum ada interaksi lain yang disetujui
        if (interaksi.getKehilanganBarang() != null) {
            boolean sudahAdaYangDisetujui =
                klaimRepository.existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                    interaksi.getKehilanganBarang().getIdKehilangan(), StatusInteraksi.DISETUJUI);

        if (sudahAdaYangDisetujui) {
                throw new RuntimeException("Sudah ada interaksi lain yang disetujui untuk laporan ini.");
            }

        } else if (interaksi.getPenemuBarang() != null) {
            boolean sudahAdaYangDisetujui =
                klaimRepository.existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                    interaksi.getPenemuBarang().getIdPenemu(), StatusInteraksi.DISETUJUI);

        if (sudahAdaYangDisetujui) {
                throw new RuntimeException("Sudah ada interaksi lain yang disetujui untuk laporan ini.");
            }
        }

        validasiRelasiLaporan(interaksi);

        if (interaksi.getKehilanganBarang() != null) {
            validasiBarangTersedia(interaksi.getKehilanganBarang().getStatus(), "Laporan kehilangan ini sudah tidak tersedia.");
        } else {
            validasiBarangTersedia(interaksi.getPenemuBarang().getStatus(), "Laporan temuan ini sudah tidak tersedia.");
        }

        // Perbarui status interaksi
        interaksi.setStatusInteraksi(StatusInteraksi.DISETUJUI);
        interaksi.setAdminVerifikator(admin);
        interaksi.setWaktuVerifikasi(LocalDateTime.now());

        KlaimAtauTanggapan interaksiTersimpan =klaimRepository.save(interaksi);

        // Tolak otomatis interaksi lain pada laporan yang sama
        if (interaksi.getKehilanganBarang() != null) {
            List<KlaimAtauTanggapan> interaksiLain =
                klaimRepository.findByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                    interaksi.getKehilanganBarang().getIdKehilangan(),StatusInteraksi.MENUNGGU_VERIFIKASI);

            tolakInteraksiLain(interaksiLain, admin, interaksi.getIdInteraksi());

        } else if (interaksi.getPenemuBarang() != null) {
            List<KlaimAtauTanggapan> interaksiLain =
                klaimRepository.findByPenemuBarang_IdPenemuAndStatusInteraksi(
                    interaksi.getPenemuBarang().getIdPenemu(),StatusInteraksi.MENUNGGU_VERIFIKASI);

            tolakInteraksiLain(interaksiLain, admin, interaksi.getIdInteraksi());
        }

        // Kirim notifikasi kepada mahasiswa yang mengajukan
        notifikasiService.buatNotifikasi(
            interaksi.getPenggunaMerespons(),
            interaksiTersimpan,
            "Interaksi Disetujui",
            "Interaksi kamu telah disetujui oleh admin.",
            JenisNotifikasi.INTERAKSI_DISETUJUI
        );

        // Catat aktivitas admin
        logAktivitasService.catatAktivitas(
            admin,
            interaksi.getKehilanganBarang(),
            interaksi.getPenemuBarang(),
            "Menyetujui interaksi ID " + idInteraksi
        );

        return interaksiTersimpan;
    }

    private void tolakInteraksiLain(
        List<KlaimAtauTanggapan> interaksiLain,
        Pengguna admin,
        Long idInteraksi) {

        for (KlaimAtauTanggapan lainnya : interaksiLain) {

            if (!lainnya.getIdInteraksi().equals(idInteraksi)) {

                lainnya.setStatusInteraksi(StatusInteraksi.DITOLAK);
                lainnya.setAdminVerifikator(admin);
                lainnya.setAlasanPenolakan("Laporan barang sudah memiliki pengajuan lain yang disetujui.");
                lainnya.setWaktuVerifikasi(LocalDateTime.now());

                klaimRepository.save(lainnya);

                notifikasiService.buatNotifikasi(
                        lainnya.getPenggunaMerespons(),
                        lainnya,
                        "Interaksi Ditolak",
                        "Pengajuan kamu ditolak karena laporan barang sudah memiliki pengajuan lain yang disetujui.",
                        JenisNotifikasi.INTERAKSI_DITOLAK
                );
            }
        }
    }
   
    @Transactional
    public KlaimAtauTanggapan tolakInteraksi(
            Long idInteraksi,
            String alasanPenolakan,
            Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = ambilAdminLogin(
            authentication,
            "Hanya admin yang dapat menolak interaksi"
        );

        // Validasi alasan penolakan
        if (alasanPenolakan == null || alasanPenolakan.isBlank()) {
            throw new RuntimeException("Alasan penolakan wajib diisi");
        }

        // Cari interaksi
        KlaimAtauTanggapan interaksi = ambilInteraksi(idInteraksi);

        // Pastikan masih menunggu verifikasi
        validasiStatusInteraksi(
                interaksi,
                StatusInteraksi.MENUNGGU_VERIFIKASI,
                "Interaksi sudah pernah diverifikasi"
        );

        validasiRelasiLaporan(interaksi);  

        // Perbarui status interaksi
        interaksi.setStatusInteraksi(StatusInteraksi.DITOLAK);
        interaksi.setAdminVerifikator(admin);
        interaksi.setAlasanPenolakan(alasanPenolakan);
        interaksi.setWaktuVerifikasi(LocalDateTime.now());

        KlaimAtauTanggapan interaksiTersimpan =klaimRepository.save(interaksi);

        // Kirim notifikasi kepada mahasiswa
        notifikasiService.buatNotifikasi(
            interaksi.getPenggunaMerespons(),
            interaksiTersimpan,
            "Interaksi Ditolak",
            "Interaksi kamu ditolak oleh admin. Alasan: " + alasanPenolakan,
            JenisNotifikasi.INTERAKSI_DITOLAK
        );

        // Catat aktivitas admin
        logAktivitasService.catatAktivitas(
            admin,
            interaksi.getKehilanganBarang(),
            interaksi.getPenemuBarang(),
            "Menolak interaksi ID " + idInteraksi
        );

        return interaksiTersimpan;
    }

    private void validasiRelasiLaporan(KlaimAtauTanggapan interaksi) {
        if ((interaksi.getKehilanganBarang() == null && interaksi.getPenemuBarang() == null)
                || (interaksi.getKehilanganBarang() != null && interaksi.getPenemuBarang() != null)) {
            throw new RuntimeException("Interaksi harus terhubung ke tepat satu laporan barang.");
        }
    }
    
    @Transactional
    public KlaimAtauTanggapan selesaikanInteraksi(
            Long idInteraksi,
            Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = ambilAdminLogin(
            authentication,
            "Hanya admin yang dapat menyelesaikan interaksi"
        );

        // Cari interaksi
        KlaimAtauTanggapan interaksi = ambilInteraksi(idInteraksi);

        // Pastikan interaksi sudah disetujui
        validasiStatusInteraksi(
            interaksi,
            StatusInteraksi.DISETUJUI,
            "Hanya interaksi yang sudah disetujui yang dapat diselesaikan"
        );

        // Pastikan interaksi hanya terhubung ke satu laporan
        validasiRelasiLaporan(interaksi);

        if (interaksi.getKehilanganBarang() != null && interaksi.getKehilanganBarang().getStatus() == StatusBarang.SELESAI) {
            throw new RuntimeException("Laporan kehilangan ini sudah diselesaikan.");
        }

        if (interaksi.getPenemuBarang() != null && interaksi.getPenemuBarang().getStatus() == StatusBarang.SELESAI) {
            throw new RuntimeException("Laporan temuan ini sudah diselesaikan.");
        }

        // Proses yang laporannya sudah diarsipkan tidak dapat dilanjutkan;
        // admin harus memulihkan laporan terlebih dahulu.
        if ((interaksi.getKehilanganBarang() != null
                && interaksi.getKehilanganBarang().getStatus() == StatusBarang.DIARSIPKAN)
            || (interaksi.getPenemuBarang() != null
                && interaksi.getPenemuBarang().getStatus() == StatusBarang.DIARSIPKAN)) {
            throw new RuntimeException(
                "Laporan ini sudah diarsipkan. Pulihkan laporan terlebih dahulu sebelum menyerahkan barang.");
        }

        // Ubah status barang menjadi selesai
        if (interaksi.getKehilanganBarang() != null) {
            interaksi.getKehilanganBarang().setStatus(StatusBarang.SELESAI);
        } else {
            interaksi.getPenemuBarang().setStatus(StatusBarang.SELESAI);
        }

        // Ubah status interaksi
        interaksi.setStatusInteraksi(StatusInteraksi.SELESAI);
        interaksi.setWaktuSelesai(LocalDateTime.now());

        KlaimAtauTanggapan interaksiTersimpan = klaimRepository.save(interaksi);

        // Kirim notifikasi kepada mahasiswa yang mengajukan
        notifikasiService.buatNotifikasi(
            interaksi.getPenggunaMerespons(),
            interaksiTersimpan,
            "Proses Pengembalian Selesai",
            "Proses penyerahan barang telah dikonfirmasi oleh admin Posko FIK.",
            JenisNotifikasi.BARANG_SELESAI
        );
        
        // Kirim notifikasi kepada pihak pemilik laporan
        Pengguna pemilikLaporan;

        if (interaksi.getKehilanganBarang() != null) {
            pemilikLaporan = interaksi.getKehilanganBarang().getPengguna();
        } else {
            pemilikLaporan = interaksi.getPenemuBarang().getPengguna();
        }

        notifikasiService.buatNotifikasi(
            pemilikLaporan,
            interaksiTersimpan,
            "Proses Pengembalian Selesai",
            "Penyerahan barang telah dikonfirmasi oleh admin Posko FIK.",
            JenisNotifikasi.BARANG_SELESAI
        );

        // Catat aktivitas admin
        logAktivitasService.catatAktivitas(
            admin,
            interaksi.getKehilanganBarang(),
            interaksi.getPenemuBarang(),
            "Menyelesaikan penyerahan barang untuk interaksi ID " + idInteraksi
        );

        return interaksiTersimpan;
    }

    @Transactional(readOnly = true)
    public List<KlaimAtauTanggapanResponseDTO> ambilSemuaInteraksi(Authentication authentication) {

        ambilAdminLogin(authentication, "Akses hanya untuk admin");

        return klaimRepository.findAllByOrderByWaktuDibuatDesc()
                .stream()
                .map(klaimMapper::toDTO)
                .toList();
    }  

    @Transactional(readOnly = true)
    public List<KlaimAtauTanggapanResponseDTO> ambilInteraksiMahasiswa(Authentication authentication) {

        Pengguna mahasiswa = ambilMahasiswaLogin(authentication);

        return klaimRepository
                .findByPenggunaMerespons_IdPenggunaOrderByWaktuDibuatDesc(mahasiswa.getIdPengguna())
                .stream()
                .map(klaimMapper::toDTO)
                .toList();
    }

    // Foto bukti pendukung bersifat opsional; disimpan setelah interaksi berhasil dibuat.
    @Transactional
    public void simpanFotoBukti(Long idInteraksi, String pathFotoBukti) {
        KlaimAtauTanggapan interaksi = klaimRepository.findById(idInteraksi)
                .orElseThrow(() -> new RuntimeException("Interaksi tidak ditemukan"));
        interaksi.setFotoBukti(pathFotoBukti);
        klaimRepository.save(interaksi);
    }
}
