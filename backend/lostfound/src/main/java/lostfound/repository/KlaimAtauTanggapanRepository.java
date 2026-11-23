package lostfound.repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
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

    // ===== Query untuk tampilan web =====

    String DETAIL = "select i from KlaimAtauTanggapan i "
            + "left join fetch i.kehilanganBarang left join fetch i.penemuBarang "
            + "join fetch i.penggunaMerespons left join fetch i.adminVerifikator ";

    @Query(DETAIL + "order by i.waktuDibuat desc")
    List<KlaimAtauTanggapan> semuaDenganDetail();

    @Query(DETAIL + "where i.penggunaMerespons.idPengguna = :idPengguna order by i.waktuDibuat desc")
    List<KlaimAtauTanggapan> milikPengguna(@Param("idPengguna") Integer idPengguna);

    @Query(DETAIL + "where i.idInteraksi = :id")
    Optional<KlaimAtauTanggapan> findDetail(@Param("id") Long id);

    long countByStatusInteraksi(StatusInteraksi statusInteraksi);

    long countByPenggunaMerespons_IdPenggunaAndStatusInteraksi(
            Integer idPengguna, StatusInteraksi statusInteraksi);

    long countByWaktuSelesaiBetween(LocalDateTime awal, LocalDateTime akhir);

    long countByKehilanganBarang_IdKehilanganAndStatusInteraksi(
            Long idKehilangan, StatusInteraksi statusInteraksi);

    long countByPenemuBarang_IdPenemuAndStatusInteraksi(
            Long idPenemu, StatusInteraksi statusInteraksi);

    boolean existsByKehilanganBarang_IdKehilanganAndPenggunaMerespons_IdPenggunaAndStatusInteraksiIn(
            Long idKehilangan, Integer idPengguna, Collection<StatusInteraksi> status);

    boolean existsByPenemuBarang_IdPenemuAndPenggunaMerespons_IdPenggunaAndStatusInteraksiIn(
            Long idPenemu, Integer idPengguna, Collection<StatusInteraksi> status);

    @Query("select i.idInteraksi from KlaimAtauTanggapan i "
            + "where i.kehilanganBarang.idKehilangan = :idBarang "
            + "and i.penggunaMerespons.idPengguna = :idPengguna "
            + "order by i.idInteraksi desc")
    List<Long> idInteraksiPenggunaPadaKehilangan(@Param("idBarang") Long idBarang,
                                                 @Param("idPengguna") Integer idPengguna);

    @Query("select i.idInteraksi from KlaimAtauTanggapan i "
            + "where i.penemuBarang.idPenemu = :idBarang "
            + "and i.penggunaMerespons.idPengguna = :idPengguna "
            + "order by i.idInteraksi desc")
    List<Long> idInteraksiPenggunaPadaTemuan(@Param("idBarang") Long idBarang,
                                             @Param("idPengguna") Integer idPengguna);

    // Semua interaksi pada satu laporan (dipakai aturan arsip otomatis)
    List<KlaimAtauTanggapan> findByKehilanganBarang_IdKehilangan(Long idKehilangan);

    List<KlaimAtauTanggapan> findByPenemuBarang_IdPenemu(Long idPenemu);
}
