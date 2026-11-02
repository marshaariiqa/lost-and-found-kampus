package lostfound.service;

import lostfound.model.Pengguna;
import lostfound.model.ProviderLogin;
import lostfound.model.PeranPengguna;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(Pengguna pengguna) {

        String nama = pengguna.getNamaLengkap().trim();
        String email = pengguna.getEmail().trim().toLowerCase();
        String password = pengguna.getKataSandi();

        if (nama.isEmpty()) {
            throw new IllegalArgumentException("Nama lengkap wajib diisi");
        }

        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email wajib diisi");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                "Password minimal 8 karakter"
            );
        }

        if (penggunaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        pengguna.setNamaLengkap(nama);
        pengguna.setEmail(email);
        pengguna.setKataSandi(passwordEncoder.encode(password));
        pengguna.setPeran(PeranPengguna.MAHASISWA);
        pengguna.setProvider(ProviderLogin.LOCAL);

        penggunaRepository.save(pengguna);
    }
}
