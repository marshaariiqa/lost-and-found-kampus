package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.service.CurrentUserService;
import lostfound.service.FileStorageService;
import lostfound.service.InteraksiViewService;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Menyajikan foto KTM dan foto bukti (privat). Hanya admin atau pemilik pengajuan
 * yang boleh membukanya.
 */
@Controller
public class FilePageController {

    private final CurrentUserService currentUserService;
    private final InteraksiViewService interaksiViewService;
    private final FileStorageService fileStorageService;

    public FilePageController(CurrentUserService currentUserService,
                              InteraksiViewService interaksiViewService,
                              FileStorageService fileStorageService) {
        this.currentUserService = currentUserService;
        this.interaksiViewService = interaksiViewService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/files/ktm/{id}")
    public ResponseEntity<Resource> ktm(@PathVariable Long id, Authentication authentication) {
        Pengguna pengguna = currentUserService.wajib(authentication);
        return kirim(interaksiViewService.pathKtmUntuk(id, pengguna));
    }

    @GetMapping("/files/bukti/{id}")
    public ResponseEntity<Resource> bukti(@PathVariable Long id, Authentication authentication) {
        Pengguna pengguna = currentUserService.wajib(authentication);
        return kirim(interaksiViewService.pathBuktiUntuk(id, pengguna));
    }

    private ResponseEntity<Resource> kirim(java.util.Optional<String> path) {
        return path
                .map(fileStorageService::muat)
                .map(resource -> {
                    String nama = resource.getFilename() == null ? "" : resource.getFilename().toLowerCase();
                    MediaType tipe = nama.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
                    return ResponseEntity.ok()
                            .contentType(tipe)
                            .cacheControl(CacheControl.noStore())
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                            .header("X-Content-Type-Options", "nosniff")
                            .body(resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
