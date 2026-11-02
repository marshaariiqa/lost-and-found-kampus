package lostfound.repository;

import lostfound.model.KehilanganBarang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KehilanganBarangRepository extends JpaRepository<KehilanganBarang, Long> {
}