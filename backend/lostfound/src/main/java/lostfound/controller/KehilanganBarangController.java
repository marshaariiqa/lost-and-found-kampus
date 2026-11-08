package lostfound.controller;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.KehilanganBarangResponseDTO;
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
    public ResponseEntity<List<KehilanganBarangResponseDTO>> getAllKehilangan() {

        List<KehilanganBarangResponseDTO> response =
                kehilanganBarangService.getAllKehilangan()
                        .stream()
                        .map(kehilanganBarangService::toResponseDTO)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KehilanganBarangResponseDTO> getKehilanganById(
            @PathVariable Long id) {

        return kehilanganBarangService.getKehilanganById(id)
                .map(kehilanganBarangService::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KehilanganBarangResponseDTO> createKehilangan(
            @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.createKehilangan(
                        dto, authentication.getName()
                );

        return ResponseEntity.ok(
                kehilanganBarangService.toResponseDTO(barang)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<KehilanganBarangResponseDTO> updateKehilangan(
            @PathVariable Long id,
            @RequestBody KehilanganBarangDTO dto,
            Authentication authentication) {

        KehilanganBarang barang =
                kehilanganBarangService.updateKehilangan(
                        id, dto, authentication.getName()
                );

        return ResponseEntity.ok(
                kehilanganBarangService.toResponseDTO(barang)
        );
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