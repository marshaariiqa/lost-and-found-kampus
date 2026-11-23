package lostfound.controller;

import lostfound.dto.LogAktivitasDTO;
import lostfound.model.LogAktivitas;
import lostfound.service.LogAktivitasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log-aktivitas")
public class LogAktivitasController {

    @Autowired
    private LogAktivitasService logAktivitasService;

    @GetMapping
    public ResponseEntity<List<LogAktivitasDTO>> ambilSemuaLog(Authentication authentication) {

        return ResponseEntity.ok(petakan(logAktivitasService.ambilSemuaLog(authentication)));
    }

    @GetMapping("/kehilangan/{idKehilangan}")
    public ResponseEntity<List<LogAktivitasDTO>> ambilLogKehilangan(
            @PathVariable Long idKehilangan,Authentication authentication) {

        return ResponseEntity.ok(petakan(logAktivitasService.ambilLogKehilangan(idKehilangan, authentication)));
    }

    @GetMapping("/penemuan/{idPenemu}")
    public ResponseEntity<List<LogAktivitasDTO>> ambilLogPenemuan(
            @PathVariable Long idPenemu,Authentication authentication) {

        return ResponseEntity.ok(petakan(logAktivitasService.ambilLogPenemuan(idPenemu, authentication)));
    }

    private List<LogAktivitasDTO> petakan(List<LogAktivitas> daftar) {
        return daftar.stream().map(l -> new LogAktivitasDTO(
                l.getIdLog(),
                l.getKehilanganBarang() == null ? null : l.getKehilanganBarang().getIdKehilangan(),
                l.getPenemuBarang() == null ? null : l.getPenemuBarang().getIdPenemu(),
                l.getAdmin().getIdPengguna(),
                l.getAdmin().getNamaLengkap(),
                l.getTindakan(),
                l.getWaktuCatat())).toList();
    }
}
