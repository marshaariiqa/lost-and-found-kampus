package lostfound.controller;

import lostfound.dto.PenemuBarangDTO;
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
    public ResponseEntity<List<PenemuBarang>> getAllTemuan() {
        return ResponseEntity.ok(
                penemuBarangService.getAllTemuan()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenemuBarang> getTemuanById(
            @PathVariable Long id) {

        return penemuBarangService.getTemuanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PenemuBarang> createTemuan(
            @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {

        PenemuBarang barang =
                penemuBarangService.createTemuan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(barang);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PenemuBarang> updateTemuan(
            @PathVariable Long id,
            @RequestBody PenemuBarangDTO dto,
            Authentication authentication) {

        PenemuBarang barang =
                penemuBarangService.updateTemuan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(barang);
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
