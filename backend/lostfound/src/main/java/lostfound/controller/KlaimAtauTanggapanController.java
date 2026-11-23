package lostfound.controller;

import lostfound.dto.KlaimAtauTanggapanResponseDTO;
import lostfound.model.KlaimAtauTanggapan;
import lostfound.service.FileStorageService;
import lostfound.service.KlaimAtauTanggapanMapper;
import lostfound.service.KlaimAtauTanggapanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/interaksi")
public class KlaimAtauTanggapanController {

    @Autowired
    private KlaimAtauTanggapanService klaimService;

    @Autowired
    private KlaimAtauTanggapanMapper klaimMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // Mahasiswa mengajukan tanggapan pada laporan kehilangan.
    // Foto KTM WAJIB dan dikirim sebagai file (multipart), bukan string path.
    @PostMapping(value = "/kehilangan/{idKehilangan}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KlaimAtauTanggapanResponseDTO> ajukanTanggapanKehilangan(
            @PathVariable Long idKehilangan,
            @RequestParam String catatanBukti,
            @RequestParam("fotoKtm") MultipartFile fotoKtm,
            Authentication authentication) {

        String pathKtm = fileStorageService.simpanFotoKtm(fotoKtm);
        try {
            KlaimAtauTanggapan interaksi = klaimService.ajukanTanggapanKehilangan(
                    idKehilangan, catatanBukti, pathKtm, authentication);
            return ResponseEntity.ok(klaimMapper.toDTO(interaksi));
        } catch (RuntimeException e) {
            fileStorageService.hapus(pathKtm);
            throw e;
        }
    }

    // Mahasiswa mengajukan klaim pada laporan barang temuan (foto KTM wajib)
    @PostMapping(value = "/temuan/{idPenemu}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KlaimAtauTanggapanResponseDTO> ajukanKlaimTemuan(
            @PathVariable Long idPenemu,
            @RequestParam String catatanBukti,
            @RequestParam("fotoKtm") MultipartFile fotoKtm,
            Authentication authentication) {

        String pathKtm = fileStorageService.simpanFotoKtm(fotoKtm);
        try {
            KlaimAtauTanggapan interaksi = klaimService.ajukanKlaimTemuan(
                    idPenemu, catatanBukti, pathKtm, authentication);
            return ResponseEntity.ok(klaimMapper.toDTO(interaksi));
        } catch (RuntimeException e) {
            fileStorageService.hapus(pathKtm);
            throw e;
        }
    }

    // Admin menyetujui interaksi
    @PutMapping("/{idInteraksi}/setujui")
    public ResponseEntity<KlaimAtauTanggapanResponseDTO> setujuiInteraksi(
            @PathVariable Long idInteraksi,
            Authentication authentication) {

        return ResponseEntity.ok(klaimMapper.toDTO(
                klaimService.setujuiInteraksi(idInteraksi, authentication)));
    }

    // Admin menolak interaksi
    @PutMapping("/{idInteraksi}/tolak")
    public ResponseEntity<KlaimAtauTanggapanResponseDTO> tolakInteraksi(
            @PathVariable Long idInteraksi,
            @RequestParam String alasanPenolakan,
            Authentication authentication) {

        return ResponseEntity.ok(klaimMapper.toDTO(
                klaimService.tolakInteraksi(idInteraksi, alasanPenolakan, authentication)));
    }

    // Admin menyelesaikan penyerahan barang
    @PutMapping("/{idInteraksi}/selesaikan")
    public ResponseEntity<KlaimAtauTanggapanResponseDTO> selesaikanInteraksi(
            @PathVariable Long idInteraksi,
            Authentication authentication) {

        return ResponseEntity.ok(klaimMapper.toDTO(
                klaimService.selesaikanInteraksi(idInteraksi, authentication)));
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
