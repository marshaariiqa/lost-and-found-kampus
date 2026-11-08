package lostfound.repository;

import lostfound.model.LogAktivitas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAktivitasRepository
        extends JpaRepository<LogAktivitas, Long> {
}