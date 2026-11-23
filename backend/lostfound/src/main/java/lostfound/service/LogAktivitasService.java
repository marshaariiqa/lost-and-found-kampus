package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.LogAktivitas;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.repository.LogAktivitasRepository;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogAktivitasService {

    @Autowired
    private LogAktivitasRepository logAktivitasRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Transactional(readOnly = true)
    public List<LogAktivitas> ambilSemuaLog(Authentication authentication) {

        validasiAdmin(authentication);

        return logAktivitasRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LogAktivitas> ambilLogKehilangan(
            Long idKehilangan,
            Authentication authentication) {

        validasiAdmin(authentication);

        return logAktivitasRepository.findByKehilanganBarang_IdKehilanganOrderByWaktuCatatDesc(idKehilangan);
    }

    @Transactional(readOnly = true)
    public List<LogAktivitas> ambilLogPenemuan(
            Long idPenemu,
            Authentication authentication) {

        validasiAdmin(authentication);

        return logAktivitasRepository.findByPenemuBarang_IdPenemuOrderByWaktuCatatDesc(idPenemu);
    }

    private void validasiAdmin(Authentication authentication) {

        Pengguna admin = penggunaRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat melihat log aktivitas");
        }
    }

    public void catatAktivitas(
        Pengguna admin,
        KehilanganBarang kehilangan,
        PenemuBarang penemu,
        String tindakan) {

        LogAktivitas log = new LogAktivitas();

        log.setAdmin(admin);
        log.setKehilanganBarang(kehilangan);
        log.setPenemuBarang(penemu);
        log.setTindakan(tindakan);

        logAktivitasRepository.save(log);
    }
}