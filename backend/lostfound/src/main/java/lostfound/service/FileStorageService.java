package lostfound.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Penyimpanan file upload.
 *
 * - Foto barang disimpan di {upload-dir}/barang dan bersifat publik
 *   (URL: /uploads/barang/{nama-file}).
 * - Foto KTM disimpan di {upload-dir}/ktm dan PRIVAT. Tidak ada resource
 *   handler untuk folder ini; file hanya dibuka lewat FileController
 *   setelah pemeriksaan hak akses.
 *
 * Validasi: hanya PNG/JPG (dicek lewat magic bytes, bukan hanya ekstensi)
 * dan maksimal 5 MB. Nama file selalu dibuat acak (UUID).
 */
@Service
public class FileStorageService {

    public static final long MAKS_BYTE = 5L * 1024 * 1024;
    public static final String URL_PREFIX_BARANG = "/uploads/barang/";

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    public Path getFolderBarang() {
        return root.resolve("barang");
    }

    /** Menyimpan foto barang. Mengembalikan URL publik, atau null bila tidak ada file. */
    public String simpanFotoBarang(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String nama = simpan(file, "barang");
        return URL_PREFIX_BARANG + nama;
    }

    /** Menyimpan foto KTM (wajib). Mengembalikan path relatif, mis. "ktm/abc.jpg". */
    public String simpanFotoKtm(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Foto KTM wajib diunggah");
        }
        return "ktm/" + simpan(file, "ktm");
    }

    /** Foto bukti pendukung (opsional, privat). Mengembalikan path relatif atau null. */
    public String simpanFotoBukti(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return "bukti/" + simpan(file, "bukti");
    }

    private String simpan(MultipartFile file, String folder) {
        if (file.getSize() > MAKS_BYTE) {
            throw new IllegalArgumentException("Ukuran file maksimal 5 MB");
        }

        String ekstensi;
        try (InputStream in = file.getInputStream()) {
            byte[] kepala = in.readNBytes(8);
            ekstensi = deteksiEkstensi(kepala);
        } catch (IOException e) {
            throw new IllegalArgumentException("File tidak dapat dibaca");
        }

        if (ekstensi == null) {
            throw new IllegalArgumentException("Format file harus PNG atau JPG");
        }

        try {
            Path folderTujuan = root.resolve(folder);
            Files.createDirectories(folderTujuan);
            String namaFile = UUID.randomUUID() + "." + ekstensi;
            Path tujuan = folderTujuan.resolve(namaFile).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tujuan, StandardCopyOption.REPLACE_EXISTING);
            }
            return namaFile;
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan file", e);
        }
    }

    /** Mengenali PNG / JPEG dari byte awal file. Mengembalikan ekstensi atau null. */
    static String deteksiEkstensi(byte[] b) {
        if (b == null) {
            return null;
        }
        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        return null;
    }

    /** Membuka file privat berdasarkan path relatif yang tersimpan di database. */
    public Resource muat(String pathRelatif) {
        if (pathRelatif == null || pathRelatif.isBlank()) {
            return null;
        }
        Path file = root.resolve(pathRelatif).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            return null;
        }
        return new PathResource(file);
    }

    /** Menghapus file (dipakai saat penyimpanan data gagal setelah upload). */
    public void hapus(String pathAtauUrl) {
        if (pathAtauUrl == null || pathAtauUrl.isBlank()) {
            return;
        }
        try {
            Path file;
            if (pathAtauUrl.startsWith(URL_PREFIX_BARANG)) {
                file = getFolderBarang()
                        .resolve(pathAtauUrl.substring(URL_PREFIX_BARANG.length())).normalize();
            } else {
                file = root.resolve(pathAtauUrl).normalize();
            }
            if (file.startsWith(root)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
            // Gagal menghapus file sisa tidak boleh menggagalkan alur utama.
        }
    }
}
