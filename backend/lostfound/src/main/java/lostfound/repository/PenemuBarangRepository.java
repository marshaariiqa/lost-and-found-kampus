package lostfound.repository;

import lostfound.model.PenemuBarang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenemuBarangRepository extends JpaRepository<PenemuBarang, Long> {
}