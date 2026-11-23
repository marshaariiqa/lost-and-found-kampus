package lostfound.service;

import lostfound.model.Notifikasi;
import lostfound.model.Pengguna;
import lostfound.repository.NotifikasiRepository;
import lostfound.util.Fmt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifikasi untuk halaman web: daftar, jumlah belum dibaca, dan
 * membuka notifikasi (tandai dibaca + tujuan tautan).
 */
@Service
public class NotifikasiViewService {

    public record NotifView(Long id, String judul, String pesan, String jenis,
                            boolean dibaca, String waktu, String kelompok) {
    }

    private final NotifikasiRepository notifikasiRepository;

    public NotifikasiViewService(NotifikasiRepository notifikasiRepository) {
        this.notifikasiRepository = notifikasiRepository;
    }

    @Transactional(readOnly = true)
    public List<NotifView> daftar(Integer idPengguna) {
        List<NotifView> hasil = new ArrayList<>();
        for (Notifikasi n : notifikasiRepository
                .findByPengguna_IdPenggunaOrderByWaktuDibuatDesc(idPengguna)) {
            hasil.add(new NotifView(
                    n.getIdNotifikasi(), n.getJudul(), n.getPesan(),
                    n.getJenisNotifikasi().name(), n.isStatusDibaca(),
                    Fmt.relatif(n.getWaktuDibuat()), Fmt.kelompokHari(n.getWaktuDibuat())));
        }
        return hasil;
    }

    @Transactional(readOnly = true)
    public long belumDibaca(Integer idPengguna) {
        return notifikasiRepository.countByPengguna_IdPenggunaAndStatusDibacaFalse(idPengguna);
    }

    /** Menandai dibaca lalu mengembalikan path tujuan. Hanya untuk pemilik notifikasi. */
    @Transactional
    public String buka(Long idNotifikasi, Pengguna pengguna) {
        Notifikasi n = notifikasiRepository.findById(idNotifikasi)
                .filter(x -> x.getPengguna().getIdPengguna().equals(pengguna.getIdPengguna()))
                .orElseThrow(() -> new IllegalArgumentException("Notifikasi tidak ditemukan"));

        if (!n.isStatusDibaca()) {
            n.setStatusDibaca(true);
            notifikasiRepository.save(n);
        }

        if (n.getInteraksi() != null
                && n.getInteraksi().getPenggunaMerespons().getIdPengguna()
                        .equals(pengguna.getIdPengguna())) {
            return "/klaim/" + n.getInteraksi().getIdInteraksi();
        }
        if (n.getKehilanganBarang() != null) {
            return "/barang/hilang/" + n.getKehilanganBarang().getIdKehilangan();
        }
        if (n.getPenemuBarang() != null) {
            return "/barang/temuan/" + n.getPenemuBarang().getIdPenemu();
        }
        return "/notifikasi";
    }
}
