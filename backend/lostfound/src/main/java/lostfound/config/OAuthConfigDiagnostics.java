package lostfound.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Mencetak, saat aplikasi start, client-id Google yang BENAR-BENAR dipakai (disamarkan)
 * dan redirect URI yang harus didaftarkan di Google Cloud Console.
 *
 * Gunanya untuk galat "Error 401: invalid_client / The OAuth client was not found":
 * penyebab paling umum adalah nilai client-id yang terbaca aplikasi berbeda dengan
 * yang ada di Google Cloud (mis. variabel environment OS lama menimpa isi .env).
 * Rahasia (client-secret) tidak pernah dicetak.
 */
@Component
public class OAuthConfigDiagnostics implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OAuthConfigDiagnostics.class);

    private final Environment env;

    public OAuthConfigDiagnostics(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String id = env.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String port = env.getProperty("server.port", "8080").trim();

        if (id.isBlank() || id.contains("${")) {
            log.warn("[OAuth] client-id Google KOSONG/belum terbaca. Isi GOOGLE_CLIENT_ID di .env.");
            return;
        }

        log.info("[OAuth] client-id yang dipakai: {} ({} karakter)", samarkan(id), id.length());
        log.info("[OAuth] Redirect URI yang HARUS terdaftar di Google Cloud Console: "
                + "http://localhost:{}/login/oauth2/code/google", port);

        if (!id.endsWith(".apps.googleusercontent.com")) {
            log.warn("[OAuth] client-id tidak berakhiran .apps.googleusercontent.com, kemungkinan salah salin.");
        }

        String dariOs = System.getenv("GOOGLE_CLIENT_ID");
        String dariFile = bacaDariFileEnv("GOOGLE_CLIENT_ID");
        if (dariOs != null && dariFile != null && !dariOs.trim().equals(dariFile.trim())) {
            log.warn("[OAuth] GOOGLE_CLIENT_ID di variabel environment OS ({}) BERBEDA dengan di file .env ({}). "
                    + "Yang dipakai aplikasi adalah variabel environment OS. Hapus variabel OS itu bila salah.",
                    samarkan(dariOs), samarkan(dariFile));
        } else if (dariOs != null) {
            log.info("[OAuth] Sumber client-id: variabel environment OS.");
        } else if (dariFile != null) {
            log.info("[OAuth] Sumber client-id: file .env.");
        }
    }

    private static String bacaDariFileEnv(String kunci) {
        try {
            Path file = Paths.get(".env");
            if (!Files.isRegularFile(file)) {
                return null;
            }
            List<String> baris = Files.readAllLines(file);
            for (String b : baris) {
                String t = b.trim();
                if (t.startsWith(kunci + "=")) {
                    return t.substring(kunci.length() + 1).trim();
                }
            }
        } catch (Exception ignored) {
            // diagnostik tidak boleh mengganggu start aplikasi
        }
        return null;
    }

    private static String samarkan(String id) {
        if (id.length() <= 20) {
            return id.substring(0, Math.min(4, id.length())) + "...";
        }
        return id.substring(0, 10) + "..." + id.substring(id.length() - 28);
    }
}
