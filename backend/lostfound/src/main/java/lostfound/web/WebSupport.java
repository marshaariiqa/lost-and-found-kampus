package lostfound.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

/**
 * Helper kecil untuk controller halaman web.
 */
public final class WebSupport {

    private static final Logger log = LoggerFactory.getLogger(WebSupport.class);

    private WebSupport() {
    }

    /**
     * Mengubah exception menjadi pesan yang aman ditampilkan ke pengguna.
     * Pesan bisnis dari service (RuntimeException biasa / IllegalArgumentException /
     * ResponseStatusException) ditampilkan apa adanya; error lain disamarkan.
     */
    public static String pesan(RuntimeException e) {
        if (e instanceof ResponseStatusException r) {
            return r.getReason() != null ? r.getReason() : "Permintaan tidak dapat diproses.";
        }
        if (e instanceof IllegalArgumentException || e.getClass() == RuntimeException.class) {
            return e.getMessage() != null ? e.getMessage() : "Permintaan tidak dapat diproses.";
        }
        log.error("Kesalahan tak terduga", e);
        return "Terjadi kesalahan pada server. Silakan coba lagi.";
    }

    public static int angka(String nilai) {
        if (nilai == null || nilai.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(nilai.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int halaman(String nilai) {
        int h = angka(nilai);
        return h < 1 ? 1 : h;
    }

    public static String bersih(String nilai) {
        return nilai == null ? "" : nilai.trim();
    }
}
