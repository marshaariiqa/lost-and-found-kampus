package lostfound.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lostfound.model.JenisNotifikasi;
import lostfound.model.KehilanganBarang;
import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.Notifikasi;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.repository.NotifikasiRepository;
import lostfound.repository.PenggunaRepository;

@Service
public class NotifikasiService {

    @Autowired
    private NotifikasiRepository notifikasiRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    private Pengguna ambilPenggunaLogin(Authentication authentication) {
        return penggunaRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));
    }

    @Transactional
    public void tandaiDibaca(Long idNotifikasi, Authentication authentication) {
        Pengguna pengguna = ambilPenggunaLogin(authentication);

        Notifikasi notifikasi = notifikasiRepository.findById(idNotifikasi)
                .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan"));

        if (!notifikasi.getPengguna().getIdPengguna().equals(pengguna.getIdPengguna())) {
            throw new RuntimeException("Notifikasi bukan milik pengguna ini");
        }

        notifikasi.setStatusDibaca(true);
        notifikasiRepository.save(notifikasi);
    }

    @Transactional(readOnly = true)
    public long hitungBelumDibaca(Authentication authentication) {
        Pengguna pengguna = ambilPenggunaLogin(authentication);

        return notifikasiRepository.countByPengguna_IdPenggunaAndStatusDibacaFalse(pengguna.getIdPengguna());
    }

    @Transactional(readOnly = true)
    public List<Notifikasi> ambilSemua(Authentication authentication) {
        Pengguna pengguna = ambilPenggunaLogin(authentication);

        return notifikasiRepository.findByPengguna_IdPenggunaOrderByWaktuDibuatDesc(pengguna.getIdPengguna());
    }

    @Transactional(readOnly = true)
    public List<Notifikasi> ambilBelumDibaca(Authentication authentication) {
        Pengguna pengguna = ambilPenggunaLogin(authentication);

        return notifikasiRepository.findByPengguna_IdPenggunaAndStatusDibacaFalseOrderByWaktuDibuatDesc(pengguna.getIdPengguna());
    }

    public void kirimNotifikasiInteraksiBaru(
        Pengguna penerima,
        KlaimAtauTanggapan interaksi,
        KehilanganBarang kehilangan,
        PenemuBarang penemu) {

        buatNotifikasi(
            penerima,
            interaksi,
            "Interaksi Baru",
            "Ada mahasiswa yang mengajukan interaksi pada laporan barang kamu.",
            JenisNotifikasi.INTERAKSI_BARU
        );
    }

    public void buatNotifikasi(
        Pengguna penerima,
        KlaimAtauTanggapan interaksi,
        String judul,
        String pesan,
        JenisNotifikasi jenis) {

        Notifikasi notifikasi = new Notifikasi();

        notifikasi.setPengguna(penerima);
        notifikasi.setInteraksi(interaksi);
        notifikasi.setKehilanganBarang(interaksi.getKehilanganBarang());
        notifikasi.setPenemuBarang(interaksi.getPenemuBarang());
        notifikasi.setJudul(judul);
        notifikasi.setPesan(pesan);
        notifikasi.setJenisNotifikasi(jenis);

        notifikasiRepository.save(notifikasi);
    }

    @Transactional
    public int tandaiSemuaDibaca(Authentication authentication) {
        Pengguna pengguna = ambilPenggunaLogin(authentication);
        return notifikasiRepository.tandaiSemuaDibaca(pengguna.getIdPengguna());
    }
}
