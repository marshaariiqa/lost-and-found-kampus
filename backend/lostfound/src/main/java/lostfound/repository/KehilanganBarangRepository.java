package lostfound.repository;

import java.util.Collection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import lostfound.model.KehilanganBarang;
import lostfound.model.StatusBarang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KehilanganBarangRepository extends JpaRepository<KehilanganBarang, Long> {

    List<KehilanganBarang> findByNamaBarangContainingIgnoreCase(String namaBarang);

    List<KehilanganBarang> findByKategori_IdKategori(Integer idKategori);

    List<KehilanganBarang> findByStatusAndWaktuDibuatBefore(
            StatusBarang status, LocalDateTime batasWaktu);

    List<KehilanganBarang> findByStatus(StatusBarang status);

    List<KehilanganBarang> findByStatusAndDihapusFalse(StatusBarang status);

    List<KehilanganBarang> findByNamaBarangContainingIgnoreCaseAndDihapusFalse(
            String namaBarang
    );

    List<KehilanganBarang> findByKategori_IdKategoriAndDihapusFalse(
            Integer idKategori
    );

    List<KehilanganBarang> findByStatusAndDihapusFalseAndWaktuDibuatBefore(
            StatusBarang status,
            LocalDateTime batasWaktu
    );

    Optional<KehilanganBarang> findByIdKehilanganAndDihapusFalse(Long id);

    // ===== Query untuk tampilan web (fetch join agar tidak N+1) =====

    @Query("select k from KehilanganBarang k join fetch k.kategori join fetch k.pengguna "
            + "where k.dihapus = false and k.status in :statuses "
            + "and lower(k.namaBarang) like lower(concat('%', :q, '%')) "
            + "and (:idKategori = 0 or k.kategori.idKategori = :idKategori) "
            + "order by k.waktuDibuat desc")
    List<KehilanganBarang> telusuri(@Param("q") String q,
                                    @Param("idKategori") int idKategori,
                                    @Param("statuses") Collection<StatusBarang> statuses);

    @Query("select k from KehilanganBarang k join fetch k.kategori join fetch k.pengguna "
            + "where k.idKehilangan = :id and k.dihapus = false")
    Optional<KehilanganBarang> findDetail(@Param("id") Long id);

    @Query("select k from KehilanganBarang k join fetch k.kategori join fetch k.pengguna "
            + "where k.pengguna.idPengguna = :idPengguna and k.dihapus = false "
            + "order by k.waktuDibuat desc")
    List<KehilanganBarang> milikPengguna(@Param("idPengguna") Integer idPengguna);

    long countByPengguna_IdPenggunaAndStatusAndDihapusFalse(Integer idPengguna, StatusBarang status);

    long countByStatusAndDihapusFalse(StatusBarang status);

    boolean existsByKategori_IdKategori(Integer idKategori);
}
