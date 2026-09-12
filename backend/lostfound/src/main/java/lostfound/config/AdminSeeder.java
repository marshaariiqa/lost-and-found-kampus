package lostfound.config;

import lostfound.model.User;
import lostfound.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public AdminSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah akun admin dengan email ini sudah ada
        String adminEmail = "admin@kampus.ac.id";
        
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User(
                "Admin Utama",
                adminEmail,
                "admin123", // Nanti akan kita enkripsi saat Spring Security dipasang
                "081234567890",
                User.Role.ROLE_ADMIN,
                User.AuthProvider.LOCAL
            );
            
            userRepository.save(admin);
            System.out.println(">>> SEEDER SUCCESS: Akun Admin Default Berhasil Dibuat! <<<");
        }
    }
}