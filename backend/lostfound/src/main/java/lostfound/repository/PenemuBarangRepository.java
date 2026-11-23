package lostfound.repository;

import java.util.Collection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import lostfound.model.PenemuBarang;
import lostfound.model.StatusBarang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PenemuBarangRepository
        extends JpaRepository<PenemuBarang, Long> {

    // Method lama
    List<PenemuBarang> findByNamaBarangContainingIgnoreCase(
            String namaBarang
    );

    List<PenemuBarang> findByKategori_IdKategori(Integer idKategori);

    List<PenemuBarang> findByStatusAndWaktuDibuatBefore(
            StatusBarang status,
            LocalDateTime batasWaktu
    );

    List<PenemuBarang> findByStatus(StatusBarang status);

    // Method yang mengecualikan laporan yang sudah dihapus
    List<PenemuBarang> findByStatusAndDihapusFalse(
            StatusBarang status
    );

    List<PenemuBarang> findByNamaBarangContainingIgnoreCaseAndDihapusFalse(
            String namaBarang
    );

    List<PenemuBarang> findByKategori_IdKategoriAndDihapusFalse(
            Integer idKategori
    );

    Optional<PenemuBarang> findByIdPenemuAndDihapusFalse(Long id);

    List<PenemuBarang> findByStatusAndDihapusFalseAndWaktuDibuatBefore(
            StatusBarang status,
            LocalDateTime batasWaktu
    );

    // ===== Query untuk tampilan web (fetch join agar tidak N+1) =====

    @Query("select p from PenemuBarang p join fetch p.kategori join fetch p.pengguna "
            + "where p.dihapus = false and p.status in :statuses "
            + "and lower(p.namaBarang) like lower(concat('%', :q, '%')) "
            + "and (:idKategori = 0 or p.kategori.idKategori = :idKategori) "
            + "order by p.waktuDibuat desc")
    List<PenemuBarang> telusuri(@Param("q") String q,
                                @Param("idKategori") int idKategori,
                                @Param("statuses") Collection<StatusBarang> statuses);

    @Query("select p from PenemuBarang p join fetch p.kategori join fetch p.pengguna "
            + "where p.idPenemu = :id and p.dihapus = false")
    Optional<PenemuBarang> findDetail(@Param("id") Long id);

    @Query("select p from PenemuBarang p join fetch p.kategori join fetch p.pengguna "
            + "where p.pengguna.idPengguna = :idPengguna and p.dihapus = false "
            + "order by p.waktuDibuat desc")
    List<PenemuBarang> milikPengguna(@Param("idPengguna") Integer idPengguna);

    long countByPengguna_IdPenggunaAndStatusAndDihapusFalse(Integer idPengguna, StatusBarang status);

    long countByStatusAndDihapusFalse(StatusBarang status);

    boolean existsByKategori_IdKategori(Integer idKategori);

    // Inventaris fisik Posko: hanya barang yang sudah diterima admin
    @Query("select p from PenemuBarang p join fetch p.kategori join fetch p.pengguna "
            + "where p.dihapus = false and p.waktuDiterima is not null and p.status in :statuses "
            + "and lower(p.namaBarang) like lower(concat('%', :q, '%')) "
            + "and (:idKategori = 0 or p.kategori.idKategori = :idKategori) "
            + "order by p.waktuDiterima desc")
    List<PenemuBarang> telusuriDiPosko(@Param("q") String q,
                                       @Param("idKategori") int idKategori,
                                       @Param("statuses") Collection<StatusBarang> statuses);

    // Laporan temuan yang belum diserahkan/diterima di Posko
    @Query("select p from PenemuBarang p join fetch p.kategori join fetch p.pengguna "
            + "where p.dihapus = false and p.waktuDiterima is null and p.status = :status "
            + "and lower(p.namaBarang) like lower(concat('%', :q, '%')) "
            + "and (:idKategori = 0 or p.kategori.idKategori = :idKategori) "
            + "order by p.waktuDibuat desc")
    List<PenemuBarang> telusuriBelumDiterima(@Param("q") String q,
                                             @Param("idKategori") int idKategori,
                                             @Param("status") StatusBarang status);

    long countByStatusAndDihapusFalseAndWaktuDiterimaIsNotNull(StatusBarang status);

    long countByStatusAndDihapusFalseAndWaktuDiterimaIsNull(StatusBarang status);
}
