package lostfound.controller;

import lostfound.service.KlaimAtauTanggapanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifikasi")
public class NotifikasiController {

    @Autowired
    private KlaimAtauTanggapanService klaimService;

    @GetMapping("/jumlah-belum-dibaca")
    public ResponseEntity<Long> hitungNotifikasiBelumDibaca(Authentication authentication) {
        return ResponseEntity.ok(klaimService.hitungNotifikasiBelumDibaca(authentication));
    }

    @PutMapping("/{idNotifikasi}/baca")
    public ResponseEntity<Void> tandaiNotifikasiDibaca(@PathVariable Long idNotifikasi, Authentication authentication) {
        klaimService.tandaiNotifikasiDibaca(idNotifikasi, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> ambilNotifikasiSaya(Authentication authentication) {
        return ResponseEntity.ok(klaimService.ambilNotifikasiSaya(authentication));
    }

    @GetMapping("/belum-dibaca")
    public ResponseEntity<?> ambilDaftarNotifikasiBelumDibaca(Authentication authentication) {
        return ResponseEntity.ok(klaimService.ambilNotifikasiBelumDibaca(authentication));
    }
}