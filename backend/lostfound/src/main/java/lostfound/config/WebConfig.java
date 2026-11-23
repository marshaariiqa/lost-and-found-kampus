package lostfound.config;

import lostfound.service.FileStorageService;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;

/**
 * Hanya foto barang (publik) yang disajikan langsung.
 * Folder foto KTM sengaja TIDAK dipetakan; akses lewat FileController.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    public WebConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String lokasi = fileStorageService.getFolderBarang().toUri().toString();
        if (!lokasi.endsWith("/")) {
            lokasi = lokasi + "/";
        }
        registry.addResourceHandler("/uploads/barang/**")
                .addResourceLocations(lokasi)
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());
    }
}
