package lostfound.controller;

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
    public ResponseEntity<List<LogAktivitas>> ambilSemuaLog(Authentication authentication) {

        return ResponseEntity.ok(logAktivitasService.ambilSemuaLog(authentication));
    }

    @GetMapping("/kehilangan/{idKehilangan}")
    public ResponseEntity<List<LogAktivitas>> ambilLogKehilangan(
            @PathVariable Long idKehilangan,Authentication authentication) {

        return ResponseEntity.ok(logAktivitasService.ambilLogKehilangan(idKehilangan, authentication));
    }

    @GetMapping("/penemuan/{idPenemu}")
    public ResponseEntity<List<LogAktivitas>> ambilLogPenemuan(
            @PathVariable Long idPenemu,Authentication authentication) {

        return ResponseEntity.ok(logAktivitasService.ambilLogPenemuan(idPenemu, authentication));
    }
}