package lostfound.repository;

import lostfound.model.Pengaturan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PengaturanRepository extends JpaRepository<Pengaturan, String> {
}
