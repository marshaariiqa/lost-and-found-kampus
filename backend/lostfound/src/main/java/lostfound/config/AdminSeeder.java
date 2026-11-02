package lostfound.config;

import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.model.ProviderLogin;
import lostfound.repository.PenggunaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final PenggunaRepository penggunaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(
            PenggunaRepository penggunaRepository,
            PasswordEncoder passwordEncoder) {
        this.penggunaRepository = penggunaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminEmail == null || adminPassword == null
                || adminEmail.isBlank() || adminPassword.isBlank()) {
            System.out.println(">>> ADMIN SEEDER: Konfigurasi admin belum tersedia. <<<");
            return;
        }

        if (!adminEmail.toLowerCase().endsWith("@upnvj.ac.id")) {
            throw new IllegalStateException(
                "Email admin harus menggunakan domain @upnvj.ac.id"
            );
        }

        if (penggunaRepository.findByEmail(adminEmail).isEmpty()) {

            Pengguna admin = new Pengguna();

            admin.setNamaLengkap("Admin Utama");
            admin.setEmail(adminEmail);
            admin.setKataSandi(passwordEncoder.encode(adminPassword));
            admin.setNomorTelepon("081234567890");
            admin.setPeran(PeranPengguna.ADMIN);
            admin.setProvider(ProviderLogin.LOCAL);

            penggunaRepository.save(admin);

            System.out.println(">>> SEEDER SUCCESS: Akun admin berhasil dibuat! <<<");
        }
    }
}