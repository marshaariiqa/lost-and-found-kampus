package lostfound.repository;

import lostfound.model.KehilanganBarang;
import lostfound.model.StatusBarang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KehilanganBarangRepository extends JpaRepository<KehilanganBarang, Long> {

    List<KehilanganBarang> findByNamaBarangContainingIgnoreCase(String namaBarang);

    List<KehilanganBarang> findByKategori_IdKategori(Integer idKategori);

    List<KehilanganBarang> findByStatusAndWaktuDibuatBefore(
            StatusBarang status, LocalDateTime batasWaktu);

    List<KehilanganBarang> findByStatus(StatusBarang status);
}