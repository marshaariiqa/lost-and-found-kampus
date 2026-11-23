package lostfound.service;

import lostfound.model.Pengguna;
import lostfound.repository.PenggunaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Mengambil pengguna yang sedang login dari Spring Security.
 * Identitas SELALU berasal dari sesi/autentikasi, tidak pernah dari request.
 * Nama autentikasi = email (form login maupun Google, lihat CustomOAuth2UserService).
 */
@Service
public class CurrentUserService {

    private final PenggunaRepository penggunaRepository;

    public CurrentUserService(PenggunaRepository penggunaRepository) {
        this.penggunaRepository = penggunaRepository;
    }

    public Optional<Pengguna> cari(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return penggunaRepository.findByEmail(authentication.getName());
    }

    public Pengguna wajib(Authentication authentication) {
        return cari(authentication).orElseThrow(
                () -> new IllegalStateException("Pengguna tidak ditemukan"));
    }
}
