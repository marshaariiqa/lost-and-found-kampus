package lostfound.controller;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.model.KehilanganBarang;
import lostfound.service.KehilanganBarangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kehilangan")
public class KehilanganBarangController {

    @Autowired
    private KehilanganBarangService kehilanganBarangService;

    @GetMapping
    public ResponseEntity<List<KehilanganBarang>> getAllKehilangan() {
        return ResponseEntity.ok(
                kehilanganBarangService.getAllKehilangan()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<KehilanganBarang> getKehilanganById(
            @PathVariable Long id) {

        return kehilanganBarangService.getKehilanganById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KehilanganBarang> createKehilangan(
            @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.createKehilangan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(barang);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KehilanganBarang> updateKehilangan(
            @PathVariable Long id,
            @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.updateKehilangan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(barang);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKehilangan(
            @PathVariable Long id,
            Authentication authentication) {

        kehilanganBarangService.deleteKehilangan(
                id, authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
