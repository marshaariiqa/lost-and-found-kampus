package lostfound.service;

import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.PenemuBarangRepository;
import lostfound.repository.PenggunaRepository;
import lostfound.repository.NotifikasiRepository;
import lostfound.repository.LogAktivitasRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.LogAktivitas;
import lostfound.model.KehilanganBarang;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.Notifikasi;
import lostfound.model.JenisNotifikasi;
import lostfound.model.StatusInteraksi;
import lostfound.model.StatusBarang;
import lostfound.model.PeranPengguna;

import lostfound.dto.KlaimAtauTanggapanResponseDTO;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

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
    private NotifikasiRepository notifikasiRepository;

    @Autowired
    private LogAktivitasRepository logAktivitasRepository;

    @Transactional
    public KlaimAtauTanggapan ajukanTanggapanKehilangan(
            Long idKehilangan,
            String catatanBukti,
            String fotoKtm,
            Authentication authentication) {

        // Ambil mahasiswa yang sedang login
        Pengguna mahasiswa = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                        new RuntimeException("Pengguna tidak ditemukan"));

        // Pastikan pengguna adalah mahasiswa
        if (mahasiswa.getPeran() != PeranPengguna.MAHASISWA) {
            throw new RuntimeException("Hanya mahasiswa yang dapat mengajukan tanggapan");
        }

        // Cari laporan kehilangan
        KehilanganBarang kehilangan = kehilanganRepository.findById(idKehilangan).orElseThrow(() ->
                        new RuntimeException("Laporan kehilangan tidak ditemukan"));

        // Pastikan bukan laporan milik sendiri
        if (kehilangan.getPengguna().getIdPengguna().equals(mahasiswa.getIdPengguna())) {
            throw new RuntimeException("Tidak dapat menanggapi laporan milik sendiri");
        }

        // Pastikan barang masih tersedia
        if (kehilangan.getStatus() != StatusBarang.TERSEDIA) {
            throw new RuntimeException("Laporan ini sudah selesai");
        }

        // Validasi catatan bukti
        if (catatanBukti == null || catatanBukti.isBlank()) {
            throw new RuntimeException("Catatan bukti wajib diisi");
        }

        // Buat interaksi baru
        KlaimAtauTanggapan interaksi = new KlaimAtauTanggapan();

        interaksi.setKehilanganBarang(kehilangan);
        interaksi.setPenemuBarang(null);
        interaksi.setPenggunaMerespons(mahasiswa);
        interaksi.setCatatanBukti(catatanBukti);
        interaksi.setFotoKtm(fotoKtm);
        interaksi.setStatusInteraksi(StatusInteraksi.MENUNGGU_VERIFIKASI);

        KlaimAtauTanggapan interaksiTersimpan =
        klaimRepository.save(interaksi);

        // Kirim notifikasi kepada pemilik laporan kehilangan
        kirimNotifikasiInteraksiBaru(
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
        Pengguna mahasiswa = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                        new RuntimeException("Pengguna tidak ditemukan"));

        // Pastikan pengguna adalah mahasiswa
        if (mahasiswa.getPeran() != PeranPengguna.MAHASISWA) {
            throw new RuntimeException("Hanya mahasiswa yang dapat mengajukan klaim");
        }

        // Cari laporan barang temuan
        PenemuBarang penemu = penemuRepository.findById(idPenemu).orElseThrow(() ->
                        new RuntimeException("Laporan barang temuan tidak ditemukan"));

        // Pastikan bukan laporan milik sendiri
        if (penemu.getPengguna().getIdPengguna().equals(mahasiswa.getIdPengguna())) {
            throw new RuntimeException("Tidak dapat mengajukan klaim pada laporan sendiri");
        }

        // Pastikan barang masih tersedia
        if (penemu.getStatus() != StatusBarang.TERSEDIA) {
            throw new RuntimeException("Barang ini sudah selesai diproses");
        }

        // Validasi catatan bukti
        if (catatanBukti == null || catatanBukti.isBlank()) {
            throw new RuntimeException("Catatan bukti wajib diisi");
        }

        // Buat interaksi baru
        KlaimAtauTanggapan interaksi = new KlaimAtauTanggapan();

        interaksi.setKehilanganBarang(null);
        interaksi.setPenemuBarang(penemu);
        interaksi.setPenggunaMerespons(mahasiswa);
        interaksi.setCatatanBukti(catatanBukti);
        interaksi.setFotoKtm(fotoKtm);
        interaksi.setStatusInteraksi(StatusInteraksi.MENUNGGU_VERIFIKASI);

        KlaimAtauTanggapan interaksiTersimpan =
        klaimRepository.save(interaksi);

        // Kirim notifikasi kepada pelapor barang temuan
        kirimNotifikasiInteraksiBaru(
                penemu.getPengguna(),
                interaksiTersimpan,
                null,
                penemu
        );

        return interaksiTersimpan;
    }
    
    private void kirimNotifikasiInteraksiBaru(
            Pengguna penerima,
            KlaimAtauTanggapan interaksi,
            KehilanganBarang kehilangan,
            PenemuBarang penemu) {

        Notifikasi notifikasi = new Notifikasi();

        notifikasi.setPengguna(penerima);
        notifikasi.setInteraksi(interaksi);
        notifikasi.setKehilanganBarang(kehilangan);
        notifikasi.setPenemuBarang(penemu);
        notifikasi.setJudul("Interaksi Baru");

        notifikasi.setPesan("Ada mahasiswa yang mengajukan interaksi pada laporan barang kamu.");

        notifikasi.setJenisNotifikasi(JenisNotifikasi.INTERAKSI_BARU);

        notifikasiRepository.save(notifikasi);
    }

    @Transactional
    public KlaimAtauTanggapan setujuiInteraksi(
            Long idInteraksi,
            Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        // Pastikan pengguna adalah admin
        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat menyetujui interaksi");
        }

        // Cari interaksi
        KlaimAtauTanggapan interaksi = klaimRepository.findById(idInteraksi).orElseThrow(() ->
                new RuntimeException("Interaksi tidak ditemukan"));

        // Pastikan interaksi masih menunggu verifikasi
        if (interaksi.getStatusInteraksi()!= StatusInteraksi.MENUNGGU_VERIFIKASI) {
            throw new RuntimeException("Interaksi sudah pernah diverifikasi");
        }

        // Pastikan belum ada interaksi lain yang disetujui
        if (interaksi.getKehilanganBarang() != null) {
            boolean sudahAdaYangDisetujui =
                    klaimRepository.existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                            interaksi.getKehilanganBarang().getIdKehilangan(), StatusInteraksi.DISETUJUI
                    );

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

        if (interaksi.getKehilanganBarang() == null && interaksi.getPenemuBarang() == null) {
                throw new RuntimeException("Interaksi tidak terhubung dengan laporan barang.");
        }

        if (interaksi.getKehilanganBarang() != null && interaksi.getPenemuBarang() != null) {
            throw new RuntimeException("Interaksi tidak boleh terhubung ke dua laporan sekaligus.");
        }

        if (interaksi.getKehilanganBarang() != null && interaksi.getKehilanganBarang().getStatus() != StatusBarang.TERSEDIA) {
            throw new RuntimeException("Laporan kehilangan ini sudah tidak tersedia.");
        }

        if (interaksi.getPenemuBarang() != null && interaksi.getPenemuBarang().getStatus() != StatusBarang.TERSEDIA) {
            throw new RuntimeException("Laporan temuan ini sudah tidak tersedia.");
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

            for (KlaimAtauTanggapan lainnya : interaksiLain) {
                if (!lainnya.getIdInteraksi().equals(interaksi.getIdInteraksi())) {

                    lainnya.setStatusInteraksi(StatusInteraksi.DITOLAK);
                    lainnya.setAdminVerifikator(admin);
                    lainnya.setAlasanPenolakan("Laporan barang sudah memiliki pengajuan lain yang disetujui.");
                    lainnya.setWaktuVerifikasi(LocalDateTime.now());

                    klaimRepository.save(lainnya);

                    Notifikasi notifikasiPenolakan = new Notifikasi();
                    notifikasiPenolakan.setPengguna(lainnya.getPenggunaMerespons());
                    notifikasiPenolakan.setInteraksi(lainnya);
                    notifikasiPenolakan.setKehilanganBarang(lainnya.getKehilanganBarang());
                    notifikasiPenolakan.setPenemuBarang(lainnya.getPenemuBarang());
                    notifikasiPenolakan.setJudul("Interaksi Ditolak");
                    notifikasiPenolakan.setPesan("Pengajuan kamu ditolak karena laporan barang sudah memiliki pengajuan lain yang disetujui.");
                    notifikasiPenolakan.setJenisNotifikasi(JenisNotifikasi.INTERAKSI_DITOLAK);
                    notifikasiRepository.save(notifikasiPenolakan);
                }
            }

        } else if (interaksi.getPenemuBarang() != null) {

            List<KlaimAtauTanggapan> interaksiLain =
                    klaimRepository.findByPenemuBarang_IdPenemuAndStatusInteraksi(
                                    interaksi.getPenemuBarang().getIdPenemu(),StatusInteraksi.MENUNGGU_VERIFIKASI);

            for (KlaimAtauTanggapan lainnya : interaksiLain) {
                if (!lainnya.getIdInteraksi().equals(interaksi.getIdInteraksi())) {

                    lainnya.setStatusInteraksi(StatusInteraksi.DITOLAK);
                    lainnya.setAdminVerifikator(admin);
                    lainnya.setAlasanPenolakan("Laporan barang sudah memiliki pengajuan lain yang disetujui.");
                    lainnya.setWaktuVerifikasi(LocalDateTime.now());

                    klaimRepository.save(lainnya);

                    Notifikasi notifikasiPenolakan = new Notifikasi();
                        notifikasiPenolakan.setPengguna(lainnya.getPenggunaMerespons());
                        notifikasiPenolakan.setInteraksi(lainnya);
                        notifikasiPenolakan.setKehilanganBarang(lainnya.getKehilanganBarang());
                        notifikasiPenolakan.setPenemuBarang(lainnya.getPenemuBarang());
                        notifikasiPenolakan.setJudul("Interaksi Ditolak");
                        notifikasiPenolakan.setPesan("Pengajuan kamu ditolak karena laporan barang sudah memiliki pengajuan lain yang disetujui.");
                        notifikasiPenolakan.setJenisNotifikasi(JenisNotifikasi.INTERAKSI_DITOLAK);
                        notifikasiRepository.save(notifikasiPenolakan);
                }
            }
        }

        // Kirim notifikasi kepada mahasiswa yang mengajukan
        Notifikasi notifikasi = new Notifikasi();
        notifikasi.setPengguna(interaksi.getPenggunaMerespons());
        notifikasi.setInteraksi(interaksiTersimpan);
        notifikasi.setKehilanganBarang(interaksi.getKehilanganBarang());
        notifikasi.setPenemuBarang(interaksi.getPenemuBarang());
        notifikasi.setJudul("Interaksi Disetujui");
        notifikasi.setPesan("Interaksi kamu telah disetujui oleh admin.");
        notifikasi.setJenisNotifikasi(JenisNotifikasi.INTERAKSI_DISETUJUI);
        notifikasiRepository.save(notifikasi);

        // Catat aktivitas admin
        LogAktivitas log = new LogAktivitas();
        log.setAdmin(admin);
        log.setKehilanganBarang(interaksi.getKehilanganBarang());
        log.setPenemuBarang(interaksi.getPenemuBarang());
        log.setTindakan("Menyetujui interaksi ID " + idInteraksi);

        logAktivitasRepository.save(log);

        return interaksiTersimpan;
    }
   
    @Transactional
    public KlaimAtauTanggapan tolakInteraksi(
            Long idInteraksi,
            String alasanPenolakan,
            Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        // Pastikan pengguna adalah admin
        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat menolak interaksi");
        }

        // Validasi alasan penolakan
        if (alasanPenolakan == null || alasanPenolakan.isBlank()) {
            throw new RuntimeException("Alasan penolakan wajib diisi");
        }

        // Cari interaksi
        KlaimAtauTanggapan interaksi = klaimRepository.findById(idInteraksi).orElseThrow(() ->
                new RuntimeException("Interaksi tidak ditemukan"));

        // Pastikan masih menunggu verifikasi
        if (interaksi.getStatusInteraksi()!= StatusInteraksi.MENUNGGU_VERIFIKASI) {
            throw new RuntimeException("Interaksi sudah pernah diverifikasi");
        }

        if ((interaksi.getKehilanganBarang() == null && interaksi.getPenemuBarang() == null)
                || (interaksi.getKehilanganBarang() != null && interaksi.getPenemuBarang() != null)) {
                throw new RuntimeException("Interaksi harus terhubung ke tepat satu laporan barang.");
        }

        // Perbarui status interaksi
        interaksi.setStatusInteraksi(StatusInteraksi.DITOLAK);
        interaksi.setAdminVerifikator(admin);
        interaksi.setAlasanPenolakan(alasanPenolakan);
        interaksi.setWaktuVerifikasi(LocalDateTime.now());

        KlaimAtauTanggapan interaksiTersimpan =klaimRepository.save(interaksi);

        // Kirim notifikasi kepada mahasiswa
        Notifikasi notifikasi = new Notifikasi();
        notifikasi.setPengguna(interaksi.getPenggunaMerespons());
        notifikasi.setInteraksi(interaksiTersimpan);
        notifikasi.setKehilanganBarang(interaksi.getKehilanganBarang());
        notifikasi.setPenemuBarang(interaksi.getPenemuBarang());
        notifikasi.setJudul("Interaksi Ditolak");
        notifikasi.setPesan("Interaksi kamu ditolak oleh admin. Alasan: "+ alasanPenolakan);
        notifikasi.setJenisNotifikasi(JenisNotifikasi.INTERAKSI_DITOLAK);

        notifikasiRepository.save(notifikasi);

        // Catat aktivitas admin
        LogAktivitas log = new LogAktivitas();
        log.setAdmin(admin);
        log.setKehilanganBarang(interaksi.getKehilanganBarang());
        log.setPenemuBarang(interaksi.getPenemuBarang());
        log.setTindakan("Menolak interaksi ID " + idInteraksi);

        logAktivitasRepository.save(log);

        return interaksiTersimpan;
    }
    
    @Transactional
    public KlaimAtauTanggapan selesaikanInteraksi(
            Long idInteraksi,
            Authentication authentication) {

        // Ambil admin yang sedang login
        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        // Pastikan pengguna adalah admin
        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat menyelesaikan interaksi");
        }

        // Cari interaksi
        KlaimAtauTanggapan interaksi = klaimRepository.findById(idInteraksi).orElseThrow(() ->
                new RuntimeException("Interaksi tidak ditemukan"));

        // Pastikan interaksi sudah disetujui
        if (interaksi.getStatusInteraksi()!= StatusInteraksi.DISETUJUI) {
            throw new RuntimeException("Hanya interaksi yang sudah disetujui yang dapat diselesaikan");
        }

        // Pastikan interaksi hanya terhubung ke satu laporan
        if ((interaksi.getKehilanganBarang() == null && interaksi.getPenemuBarang() == null)
                || (interaksi.getKehilanganBarang() != null && interaksi.getPenemuBarang() != null)) {
            throw new RuntimeException("Relasi laporan pada interaksi tidak valid.");
        }

        if (interaksi.getKehilanganBarang() != null && interaksi.getKehilanganBarang().getStatus() == StatusBarang.SELESAI) {
            throw new RuntimeException("Laporan kehilangan ini sudah diselesaikan.");
        }

        if (interaksi.getPenemuBarang() != null && interaksi.getPenemuBarang().getStatus() == StatusBarang.SELESAI) {
            throw new RuntimeException("Laporan temuan ini sudah diselesaikan.");
        }

        // Ubah status barang menjadi selesai
        if (interaksi.getKehilanganBarang() != null) {
            interaksi.getKehilanganBarang().setStatus(StatusBarang.SELESAI);
        } else if (interaksi.getPenemuBarang() != null) {
            interaksi.getPenemuBarang().setStatus(StatusBarang.SELESAI);
        } else {
            throw new RuntimeException("Interaksi tidak terhubung dengan laporan barang");
        }

        // Ubah status interaksi
        interaksi.setStatusInteraksi(StatusInteraksi.SELESAI);

        KlaimAtauTanggapan interaksiTersimpan = klaimRepository.save(interaksi);

        // Kirim notifikasi kepada mahasiswa yang mengajukan
        Notifikasi notifikasi = new Notifikasi();
        notifikasi.setPengguna(interaksi.getPenggunaMerespons());
        notifikasi.setInteraksi(interaksiTersimpan);
        notifikasi.setKehilanganBarang(interaksi.getKehilanganBarang());
        notifikasi.setPenemuBarang(interaksi.getPenemuBarang());
        notifikasi.setJudul("Proses Pengembalian Selesai");
        notifikasi.setPesan("Proses penyerahan barang telah dikonfirmasi oleh admin Posko FIK.");
        notifikasi.setJenisNotifikasi(JenisNotifikasi.BARANG_SELESAI);

        notifikasiRepository.save(notifikasi);
        
        // Kirim notifikasi kepada pihak pemilik laporan
        Pengguna pemilikLaporan = null;

        if (interaksi.getKehilanganBarang() != null) {
            pemilikLaporan = interaksi.getKehilanganBarang().getPengguna();
        } else if (interaksi.getPenemuBarang() != null) {
            pemilikLaporan = interaksi.getPenemuBarang().getPengguna();
        }

        if (pemilikLaporan != null) {
            Notifikasi notifikasiPemilik = new Notifikasi();

            notifikasiPemilik.setPengguna(pemilikLaporan);
            notifikasiPemilik.setInteraksi(interaksiTersimpan);
            notifikasiPemilik.setKehilanganBarang(interaksi.getKehilanganBarang());
            notifikasiPemilik.setPenemuBarang(interaksi.getPenemuBarang());
            notifikasiPemilik.setJudul("Proses Pengembalian Selesai");
            notifikasiPemilik.setPesan("Penyerahan barang telah dikonfirmasi oleh admin Posko FIK.");
            notifikasiPemilik.setJenisNotifikasi(JenisNotifikasi.BARANG_SELESAI);

            notifikasiRepository.save(notifikasiPemilik);
        }

        // Catat aktivitas admin
        LogAktivitas log = new LogAktivitas();
        log.setAdmin(admin);
        log.setKehilanganBarang(interaksi.getKehilanganBarang());
        log.setPenemuBarang(interaksi.getPenemuBarang());
        log.setTindakan("Menyelesaikan penyerahan barang untuk interaksi ID " + idInteraksi);

        logAktivitasRepository.save(log);

        return interaksiTersimpan;
    }
    
    @Transactional
    public void tandaiNotifikasiDibaca(
            Long idNotifikasi,
            Authentication authentication) {

        // Ambil pengguna yang sedang login
        Pengguna pengguna = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        // Cari notifikasi
        Notifikasi notifikasi = notifikasiRepository.findById(idNotifikasi).orElseThrow(() ->
                new RuntimeException("Notifikasi tidak ditemukan"));

        // Pastikan notifikasi milik pengguna yang sedang login
        if (!notifikasi.getPengguna().getIdPengguna().equals(pengguna.getIdPengguna())) {
            throw new RuntimeException("Kamu tidak memiliki akses ke notifikasi ini");
        }

        // Tandai sebagai sudah dibaca
        notifikasi.setStatusDibaca(true);

        notifikasiRepository.save(notifikasi);
    }
    
    public long hitungNotifikasiBelumDibaca(Authentication authentication) {

        Pengguna pengguna = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        return notifikasiRepository.countByPengguna_IdPenggunaAndStatusDibacaFalse(pengguna.getIdPengguna());
    }

    @Transactional(readOnly = true)
        public List<KlaimAtauTanggapanResponseDTO> ambilSemuaInteraksi(Authentication authentication) {

        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

                if (admin.getPeran() != PeranPengguna.ADMIN) {
                        throw new RuntimeException("Akses hanya untuk admin");
                }

                return klaimRepository.findAllByOrderByWaktuDibuatDesc().stream().map(this::ubahKeDTO).toList();
        }

        @Transactional(readOnly = true)
        public List<KlaimAtauTanggapanResponseDTO> ambilInteraksiMahasiswa(Authentication authentication) {

        Pengguna mahasiswa = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

                if (mahasiswa.getPeran() != PeranPengguna.MAHASISWA) {
                        throw new RuntimeException("Akses hanya untuk mahasiswa");
                }

                return klaimRepository.findByPenggunaMerespons_IdPenggunaOrderByWaktuDibuatDesc(
                        mahasiswa.getIdPengguna())
                        .stream()
                        .map(this::ubahKeDTO)
                        .toList();
        }

        private KlaimAtauTanggapanResponseDTO ubahKeDTO(KlaimAtauTanggapan interaksi) {

        KlaimAtauTanggapanResponseDTO dto =new KlaimAtauTanggapanResponseDTO();

        dto.setIdInteraksi(interaksi.getIdInteraksi());

                if (interaksi.getKehilanganBarang() != null) {
                        dto.setIdKehilangan(interaksi.getKehilanganBarang().getIdKehilangan());
                }

                if (interaksi.getPenemuBarang() != null) {
                        dto.setIdPenemu(interaksi.getPenemuBarang().getIdPenemu());
                }

        dto.setIdPenggunaMerespons(interaksi.getPenggunaMerespons().getIdPengguna());
        dto.setNamaPenggunaMerespons(interaksi.getPenggunaMerespons().getNamaLengkap());
        dto.setCatatanBukti(interaksi.getCatatanBukti());
        dto.setFotoKtm(interaksi.getFotoKtm());
        dto.setAlasanPenolakan(interaksi.getAlasanPenolakan());
        dto.setStatusInteraksi(interaksi.getStatusInteraksi().name());
        dto.setWaktuDibuat(interaksi.getWaktuDibuat());
        dto.setWaktuVerifikasi(interaksi.getWaktuVerifikasi());

        return dto;
        }

        @Transactional(readOnly = true)
        public List<Notifikasi> ambilNotifikasiSaya(Authentication authentication) {

        Pengguna pengguna = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        return notifikasiRepository.findByPengguna_IdPenggunaOrderByWaktuDibuatDesc(pengguna.getIdPengguna());
        }

        @Transactional(readOnly = true)
        public List<Notifikasi> ambilNotifikasiBelumDibaca(Authentication authentication) {

        Pengguna pengguna = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new RuntimeException("Pengguna tidak ditemukan"));

        return notifikasiRepository.findByPengguna_IdPenggunaAndStatusDibacaFalseOrderByWaktuDibuatDesc(pengguna.getIdPengguna());
        }
}