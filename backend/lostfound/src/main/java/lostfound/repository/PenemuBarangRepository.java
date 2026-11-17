package lostfound.repository;

import lostfound.model.PenemuBarang;
import lostfound.model.StatusBarang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenemuBarangRepository extends JpaRepository<PenemuBarang, Long> {

    List<PenemuBarang> findByNamaBarangContainingIgnoreCase(String namaBarang);

    List<PenemuBarang> findByKategori_IdKategori(Integer idKategori);

    List<PenemuBarang> findByStatusAndWaktuDibuatBefore(
            StatusBarang status, LocalDateTime batasWaktu);

    List<PenemuBarang> findByStatus(StatusBarang status);
}
