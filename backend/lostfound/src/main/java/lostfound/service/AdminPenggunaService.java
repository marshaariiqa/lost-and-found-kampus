package lostfound.service;

import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.model.ProviderLogin;
import lostfound.repository.PenggunaRepository;
import lostfound.util.Fmt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manajemen pengguna oleh admin: melihat daftar, mengubah peran, dan
 * mengaktifkan/menonaktifkan akun.
 */
@Service
public class AdminPenggunaService {

    public record PenggunaRow(Integer id, String nama, String inisial, String email,
                              String telepon, String peran, String provider, boolean aktif) {

        public boolean admin() {
            return "ADMIN".equals(peran);
        }
    }

    private final PenggunaRepository penggunaRepository;

    public AdminPenggunaService(PenggunaRepository penggunaRepository) {
        this.penggunaRepository = penggunaRepository;
    }

    @Transactional(readOnly = true)
    public List<PenggunaRow> daftar(String q, String peran) {
        String kunci = q == null ? "" : q.trim().toLowerCase();
        String filterPeran = peran == null ? "" : peran.trim().toUpperCase();
        List<PenggunaRow> hasil = new ArrayList<>();
        for (Pengguna p : penggunaRepository.findAllByOrderByNamaLengkapAsc()) {
            if (!filterPeran.isEmpty() && !p.getPeran().name().equals(filterPeran)) {
                continue;
            }
            if (!kunci.isEmpty()
                    && !p.getNamaLengkap().toLowerCase().contains(kunci)
                    && !p.getEmail().toLowerCase().contains(kunci)) {
                continue;
            }
            hasil.add(baris(p));
        }
        return hasil;
    }

    @Transactional(readOnly = true)
    public Optional<PenggunaRow> cari(Integer id) {
        return penggunaRepository.findById(id).map(AdminPenggunaService::baris);
    }

    @Transactional(readOnly = true)
    public long hitung() {
        return penggunaRepository.count();
    }

    @Transactional
    public void perbarui(Integer idTarget, String peranBaru, boolean aktifBaru, Pengguna adminLogin) {
        Pengguna target = penggunaRepository.findById(idTarget)
                .orElseThrow(() -> new IllegalArgumentException("Pengguna tidak ditemukan"));

        PeranPengguna peran;
        try {
            peran = PeranPengguna.valueOf(peranBaru == null ? "" : peranBaru.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Peran tidak valid");
        }

        boolean dirinyaSendiri = target.getIdPengguna().equals(adminLogin.getIdPengguna());
        if (dirinyaSendiri && (peran != PeranPengguna.ADMIN || !aktifBaru)) {
            throw new IllegalArgumentException(
                "Kamu tidak dapat menurunkan peran atau menonaktifkan akunmu sendiri");
        }

        target.setPeran(peran);
        target.setAktif(aktifBaru);
        penggunaRepository.save(target);
    }

    private static PenggunaRow baris(Pengguna p) {
        return new PenggunaRow(
                p.getIdPengguna(),
                p.getNamaLengkap(),
                Fmt.inisial(p.getNamaLengkap()),
                p.getEmail(),
                p.getNomorTelepon() == null || p.getNomorTelepon().isBlank() ? "-" : p.getNomorTelepon(),
                p.getPeran().name(),
                p.getProvider() == ProviderLogin.GOOGLE ? "Google" : "Email & kata sandi",
                p.isAktif());
    }
}
