package lostfound.service;

import lostfound.model.LogAktivitas;
import lostfound.model.Pengguna;
import lostfound.model.PeranPengguna;
import lostfound.repository.LogAktivitasRepository;
import lostfound.repository.PenggunaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogAktivitasService {

    @Autowired
    private LogAktivitasRepository logAktivitasRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    public List<LogAktivitas> ambilSemuaLog(Authentication authentication) {

        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
            new RuntimeException("Pengguna tidak ditemukan"));

        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat melihat log aktivitas");
        }

        return logAktivitasRepository.findAll();
    }

    public List<LogAktivitas> ambilLogKehilangan(
            Long idKehilangan,
            Authentication authentication) {

        validasiAdmin(authentication);

        return logAktivitasRepository.findByKehilanganBarang_IdKehilanganOrderByWaktuCatatDesc(idKehilangan);
    }

    public List<LogAktivitas> ambilLogPenemuan(
            Long idPenemu,
            Authentication authentication) {

        validasiAdmin(authentication);

        return logAktivitasRepository.findByPenemuBarang_IdPenemuOrderByWaktuCatatDesc(idPenemu);
    }

    private void validasiAdmin(Authentication authentication) {

        Pengguna admin = penggunaRepository.findByEmail(authentication.getName()).orElseThrow(() ->
            new RuntimeException("Pengguna tidak ditemukan"));

        if (admin.getPeran() != PeranPengguna.ADMIN) {
            throw new RuntimeException("Hanya admin yang dapat melihat log aktivitas");
        }
    }
}