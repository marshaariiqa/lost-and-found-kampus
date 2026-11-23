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

        if (!email.endsWith("@upnvj.ac.id")
                && !email.endsWith("@mahasiswa.upnvj.ac.id")) {
            throw new IllegalArgumentException(
                "Registrasi hanya diperbolehkan menggunakan email kampus UPNVJ."
            );
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

    // Memperbarui nama & nomor telepon pengguna yang sedang login
    public void perbaruiProfil(String emailLogin, String namaBaru, String teleponBaru) {
        Pengguna pengguna = penggunaRepository.findByEmail(emailLogin)
                .orElseThrow(() -> new IllegalArgumentException("Pengguna tidak ditemukan"));

        String nama = namaBaru == null ? "" : namaBaru.trim();
        String telepon = teleponBaru == null ? "" : teleponBaru.trim();

        if (nama.isEmpty()) {
            throw new IllegalArgumentException("Nama lengkap wajib diisi");
        }
        if (nama.length() > 150) {
            throw new IllegalArgumentException("Nama lengkap maksimal 150 karakter");
        }
        if (!telepon.isEmpty() && !telepon.matches("^[0-9+\\-\\s]{8,20}$")) {
            throw new IllegalArgumentException(
                "Nomor telepon hanya boleh berisi angka, spasi, + dan -, panjang 8 sampai 20 karakter");
        }

        pengguna.setNamaLengkap(nama);
        pengguna.setNomorTelepon(telepon.isEmpty() ? null : telepon);
        penggunaRepository.save(pengguna);
    }

    // Mengubah kata sandi akun LOCAL
    public void ubahKataSandi(String emailLogin, String lama, String baru, String konfirmasi) {
        Pengguna pengguna = penggunaRepository.findByEmail(emailLogin)
                .orElseThrow(() -> new IllegalArgumentException("Pengguna tidak ditemukan"));

        if (pengguna.getKataSandi() == null) {
            throw new IllegalArgumentException(
                "Akun ini masuk lewat Google dan tidak memiliki kata sandi");
        }
        if (lama == null || !passwordEncoder.matches(lama, pengguna.getKataSandi())) {
            throw new IllegalArgumentException("Kata sandi saat ini tidak sesuai");
        }
        if (baru == null || baru.length() < 8) {
            throw new IllegalArgumentException("Kata sandi baru minimal 8 karakter");
        }
        if (!baru.equals(konfirmasi)) {
            throw new IllegalArgumentException("Konfirmasi kata sandi belum sama");
        }

        pengguna.setKataSandi(passwordEncoder.encode(baru));
        penggunaRepository.save(pengguna);
    }
}
