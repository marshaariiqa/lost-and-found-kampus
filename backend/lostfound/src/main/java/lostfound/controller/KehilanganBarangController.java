package lostfound.controller;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.KehilanganBarangResponseDTO;
import lostfound.model.KehilanganBarang;
import lostfound.service.KehilanganBarangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/kehilangan")
public class KehilanganBarangController {

    @Autowired
    private KehilanganBarangService kehilanganBarangService;

    // Mengambil semua laporan kehilangan aktif
    @GetMapping
    public ResponseEntity<List<KehilanganBarangResponseDTO>> getAllKehilangan() {

        List<KehilanganBarangResponseDTO> response =
                kehilanganBarangService.getAllKehilangan()
                        .stream()
                        .map(kehilanganBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mengambil semua laporan kehilangan yang diarsipkan
    @GetMapping("/arsip")
    public ResponseEntity<List<KehilanganBarangResponseDTO>> getArsipKehilangan() {

        List<KehilanganBarangResponseDTO> response =
                kehilanganBarangService.getArsipKehilangan()
                        .stream()
                        .map(kehilanganBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mencari laporan berdasarkan nama barang
    @GetMapping("/cari")
    public ResponseEntity<List<KehilanganBarangResponseDTO>> cariKehilangan(
            @RequestParam String namaBarang) {

        List<KehilanganBarangResponseDTO> response =
                kehilanganBarangService.cariKehilangan(namaBarang)
                        .stream()
                        .map(kehilanganBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Filter laporan berdasarkan kategori
    @GetMapping("/kategori/{idKategori}")
    public ResponseEntity<List<KehilanganBarangResponseDTO>> filterByKategori(
            @PathVariable Integer idKategori) {

        List<KehilanganBarangResponseDTO> response =
                kehilanganBarangService.filterKehilanganByKategori(idKategori)
                        .stream()
                        .map(kehilanganBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mengambil detail laporan berdasarkan ID
    @GetMapping("/{id}")
    public ResponseEntity<KehilanganBarangResponseDTO> getKehilanganById(
            @PathVariable Long id) {

        return kehilanganBarangService.getKehilanganById(id)
                .map(kehilanganBarangService::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Membuat laporan kehilangan
    @PostMapping
    public ResponseEntity<KehilanganBarangResponseDTO> createKehilangan(
            @Valid @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.createKehilangan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(
                kehilanganBarangService.toResponseDTO(barang)
        );
    }

    // Mengubah laporan kehilangan
    @PutMapping("/{id}")
    public ResponseEntity<KehilanganBarangResponseDTO> updateKehilangan(
            @PathVariable Long id,
            @Valid @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.updateKehilangan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(
                kehilanganBarangService.toResponseDTO(barang)
        );
    }

    // Menghapus laporan kehilangan
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKehilangan(
            @PathVariable Long id,
            Authentication authentication) {

        kehilanganBarangService.deleteKehilangan(
                id, authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/arsip/{id}/restore")
    public ResponseEntity<?> restoreKehilangan(@PathVariable Long id) {
        KehilanganBarang barang = kehilanganBarangService.restoreKehilangan(id);
        return ResponseEntity.ok(kehilanganBarangService.toResponseDTO(barang));
    }
}