package lostfound.controller;

import lostfound.dto.PenemuBarangDTO;
import lostfound.dto.PenemuBarangResponseDTO;
import lostfound.model.PenemuBarang;
import lostfound.service.PenemuBarangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temuan")
public class PenemuBarangController {

    @Autowired
    private PenemuBarangService penemuBarangService;

    @GetMapping
    public ResponseEntity<List<PenemuBarangResponseDTO>> getAllTemuan() {

        List<PenemuBarangResponseDTO> response =
                penemuBarangService.getAllTemuan()
                        .stream()
                        .map(penemuBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenemuBarangResponseDTO> getTemuanById(
            @PathVariable Long id) {

        return penemuBarangService.getTemuanById(id)
                .map(penemuBarangService::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PenemuBarangResponseDTO> createTemuan(
            @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {

        PenemuBarang barang =
                penemuBarangService.createTemuan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(
                penemuBarangService.toResponseDTO(barang)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PenemuBarangResponseDTO> updateTemuan(
            @PathVariable Long id,
            @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {

        PenemuBarang barang =
                penemuBarangService.updateTemuan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(
                penemuBarangService.toResponseDTO(barang)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemuan(
            @PathVariable Long id,
            Authentication authentication) {

        penemuBarangService.deleteTemuan(
                id, authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}