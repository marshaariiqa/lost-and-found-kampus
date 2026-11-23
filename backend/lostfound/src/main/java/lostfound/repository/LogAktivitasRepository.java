package lostfound.repository;

import lostfound.model.LogAktivitas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAktivitasRepository extends JpaRepository<LogAktivitas, Long> {

    List<LogAktivitas> findByKehilanganBarang_IdKehilanganOrderByWaktuCatatDesc(Long idKehilangan);

    List<LogAktivitas> findByPenemuBarang_IdPenemuOrderByWaktuCatatDesc(Long idPenemu);

    List<LogAktivitas> findTop6ByOrderByWaktuCatatDesc();
}
