package lostfound.controller;

import lostfound.dto.PenemuBarangDTO;
import lostfound.dto.PenemuBarangResponseDTO;
import lostfound.model.PenemuBarang;
import lostfound.service.PenemuBarangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/temuan")
public class PenemuBarangController {

    @Autowired
    private PenemuBarangService penemuBarangService;

    // Mengambil semua laporan barang temuan aktif
    @GetMapping
    public ResponseEntity<List<PenemuBarangResponseDTO>> getAllTemuan() {
        List<PenemuBarangResponseDTO> response =
                penemuBarangService.getAllTemuan()
                        .stream()
                        .map(penemuBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mengambil semua laporan barang temuan yang diarsipkan
    @GetMapping("/arsip")
    public ResponseEntity<List<PenemuBarangResponseDTO>> getArsipTemuan() {
        List<PenemuBarangResponseDTO> response =
                penemuBarangService.getArsipTemuan()
                        .stream()
                        .map(penemuBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mencari laporan berdasarkan nama barang
    @GetMapping("/cari")
    public ResponseEntity<List<PenemuBarangResponseDTO>> cariTemuan(
            @RequestParam String namaBarang) {
        List<PenemuBarangResponseDTO> response =
                penemuBarangService.cariTemuan(namaBarang)
                        .stream()
                        .map(penemuBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Filter laporan berdasarkan kategori
    @GetMapping("/kategori/{idKategori}")
    public ResponseEntity<List<PenemuBarangResponseDTO>> filterByKategori(
            @PathVariable Integer idKategori) {
        List<PenemuBarangResponseDTO> response =
                penemuBarangService.filterTemuanByKategori(idKategori)
                        .stream()
                        .map(penemuBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // Mengambil detail laporan berdasarkan ID
    @GetMapping("/{id}")
    public ResponseEntity<PenemuBarangResponseDTO> getTemuanById(
            @PathVariable Long id) {
        return penemuBarangService.getTemuanById(id)
                .map(penemuBarangService::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Membuat laporan barang temuan
    @PostMapping
    public ResponseEntity<PenemuBarangResponseDTO> createTemuan(
            @Valid @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {
        PenemuBarang barang =
                penemuBarangService.createTemuan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(
                penemuBarangService.toResponseDTO(barang)
        );
    }

    // Mengubah laporan barang temuan
    @PutMapping("/{id}")
    public ResponseEntity<PenemuBarangResponseDTO> updateTemuan(
            @PathVariable Long id,
            @Valid @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {
        PenemuBarang barang =
                penemuBarangService.updateTemuan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(
                penemuBarangService.toResponseDTO(barang)
        );
    }

    // Menghapus laporan barang temuan
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemuan(
            @PathVariable Long id,
            Authentication authentication) {
        penemuBarangService.deleteTemuan(
                id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/arsip/{id}/restore")
    public ResponseEntity<PenemuBarangResponseDTO> restoreTemuan(@PathVariable Long id) {
        PenemuBarang barang = penemuBarangService.restoreTemuan(id);

        return ResponseEntity.ok(penemuBarangService.toResponseDTO(barang));
    }
}