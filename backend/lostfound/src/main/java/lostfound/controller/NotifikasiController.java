package lostfound.controller;

import lostfound.service.NotifikasiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifikasi")
public class NotifikasiController {

    @Autowired
    private NotifikasiService notifikasiService;

    @GetMapping("/jumlah-belum-dibaca")
    public ResponseEntity<Long> hitungNotifikasiBelumDibaca(Authentication authentication) {
        return ResponseEntity.ok(notifikasiService.hitungBelumDibaca(authentication));
    }

    @PutMapping("/{idNotifikasi}/baca")
    public ResponseEntity<Void> tandaiNotifikasiDibaca(@PathVariable Long idNotifikasi, Authentication authentication) {
        notifikasiService.tandaiDibaca(idNotifikasi, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> ambilNotifikasiSaya(Authentication authentication) {
        return ResponseEntity.ok(notifikasiService.ambilSemua(authentication));
    }

    @GetMapping("/belum-dibaca")
    public ResponseEntity<?> ambilDaftarNotifikasiBelumDibaca(Authentication authentication) {
        return ResponseEntity.ok(notifikasiService.ambilBelumDibaca(authentication));
    }
}