package lostfound.repository;

import lostfound.model.Notifikasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotifikasiRepository
        extends JpaRepository<Notifikasi, Long> {

        List<Notifikasi> findByPengguna_IdPenggunaOrderByWaktuDibuatDesc(
                Integer idPengguna);

        List<Notifikasi> findByPengguna_IdPenggunaAndStatusDibacaFalseOrderByWaktuDibuatDesc(
                Integer idPengguna);
        long countByPengguna_IdPenggunaAndStatusDibacaFalse(
                Integer idPengguna);
}