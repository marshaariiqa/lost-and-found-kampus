package lostfound.repository;

import lostfound.model.Pengguna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PenggunaRepository extends JpaRepository<Pengguna, Integer> {

    Optional<Pengguna> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Pengguna> findByGoogleId(String googleId);
}