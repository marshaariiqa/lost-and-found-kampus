package lostfound.repository;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.StatusInteraksi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlaimAtauTanggapanRepository
        extends JpaRepository<KlaimAtauTanggapan, Long> {

        boolean existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                Long idKehilangan,
                StatusInteraksi statusInteraksi);

        boolean existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                Long idPenemu,
                StatusInteraksi statusInteraksi);

        List<KlaimAtauTanggapan> findByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                Long idKehilangan,
                StatusInteraksi statusInteraksi);

        List<KlaimAtauTanggapan> findByPenemuBarang_IdPenemuAndStatusInteraksi(
                Long idPenemu,
                StatusInteraksi statusInteraksi);
                
        List<KlaimAtauTanggapan> findAllByOrderByWaktuDibuatDesc();

        List<KlaimAtauTanggapan> findByPenggunaMerespons_IdPenggunaOrderByWaktuDibuatDesc(
                Integer idPengguna);

}