package lostfound.controller;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.service.KlaimAtauTanggapanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interaksi")
public class KlaimAtauTanggapanController {

    @Autowired
    private KlaimAtauTanggapanService klaimService;

    // Mahasiswa mengajukan tanggapan pada laporan kehilangan
    @PostMapping("/kehilangan/{idKehilangan}")
    public ResponseEntity<KlaimAtauTanggapan> ajukanTanggapanKehilangan(
            @PathVariable Long idKehilangan,
            @RequestParam String catatanBukti,
            @RequestParam(required = false) String fotoKtm,
            Authentication authentication) {

        KlaimAtauTanggapan interaksi =klaimService.ajukanTanggapanKehilangan(
            idKehilangan,
            catatanBukti,
            fotoKtm,
            authentication
        );

        return ResponseEntity.ok(interaksi);
    }

    // Mahasiswa mengajukan klaim pada laporan barang temuan
    @PostMapping("/temuan/{idPenemu}")
    public ResponseEntity<KlaimAtauTanggapan> ajukanKlaimTemuan(
            @PathVariable Long idPenemu,
            @RequestParam String catatanBukti,
            @RequestParam(required = false) String fotoKtm,
            Authentication authentication) {

        KlaimAtauTanggapan interaksi = klaimService.ajukanKlaimTemuan(
            idPenemu,
            catatanBukti,
            fotoKtm,
            authentication
        );

        return ResponseEntity.ok(interaksi);
    }

    // Admin menyetujui interaksi
    @PutMapping("/{idInteraksi}/setujui")
    public ResponseEntity<KlaimAtauTanggapan> setujuiInteraksi(
            @PathVariable Long idInteraksi,
            Authentication authentication) {

        KlaimAtauTanggapan interaksi = klaimService.setujuiInteraksi(
            idInteraksi,
            authentication
        );

        return ResponseEntity.ok(interaksi);
    }

    // Admin menolak interaksi
    @PutMapping("/{idInteraksi}/tolak")
    public ResponseEntity<KlaimAtauTanggapan> tolakInteraksi(
            @PathVariable Long idInteraksi,
            @RequestParam String alasanPenolakan,
            Authentication authentication) {

        KlaimAtauTanggapan interaksi = klaimService.tolakInteraksi(
            idInteraksi,
            alasanPenolakan,
            authentication
        );

        return ResponseEntity.ok(interaksi);
    }

    // Admin menyelesaikan penyerahan barang
    @PutMapping("/{idInteraksi}/selesaikan")
    public ResponseEntity<KlaimAtauTanggapan> selesaikanInteraksi(
            @PathVariable Long idInteraksi,
            Authentication authentication) {

        KlaimAtauTanggapan interaksi = klaimService.selesaikanInteraksi(
            idInteraksi,
            authentication
        );

        return ResponseEntity.ok(interaksi);
    }

    @GetMapping
    public ResponseEntity<?> ambilSemuaInteraksi(Authentication authentication) {
        return ResponseEntity.ok(klaimService.ambilSemuaInteraksi(authentication));
    }

    @GetMapping("/saya")
    public ResponseEntity<?> ambilInteraksiSaya(Authentication authentication) {
        return ResponseEntity.ok(klaimService.ambilInteraksiMahasiswa(authentication));
    }
}