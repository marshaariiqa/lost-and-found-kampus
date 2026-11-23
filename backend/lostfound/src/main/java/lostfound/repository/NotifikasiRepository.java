package lostfound.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import lostfound.model.Notifikasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {

        List<Notifikasi> findByPengguna_IdPenggunaOrderByWaktuDibuatDesc(
                Integer idPengguna);

        List<Notifikasi> findByPengguna_IdPenggunaAndStatusDibacaFalseOrderByWaktuDibuatDesc(
                Integer idPengguna);
        long countByPengguna_IdPenggunaAndStatusDibacaFalse(
                Integer idPengguna);

        @Modifying
        @Query("update Notifikasi n set n.statusDibaca = true "
                + "where n.pengguna.idPengguna = :idPengguna and n.statusDibaca = false")
        int tandaiSemuaDibaca(@Param("idPengguna") Integer idPengguna);
}
