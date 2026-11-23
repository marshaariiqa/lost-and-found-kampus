package lostfound.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

class FileStorageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3};

    @TempDir
    Path folder;

    @Test
    void fotoKtmWajibAda() {
        FileStorageService s = new FileStorageService(folder.toString());
        assertThatThrownBy(() -> s.simpanFotoKtm(null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Foto KTM wajib");
        assertThatThrownBy(() -> s.simpanFotoKtm(new MockMultipartFile("fotoKtm", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Foto KTM wajib");
    }

    @Test
    void menerimaPngDanJpgDanMenyimpanKtmDiFolderPrivat() throws Exception {
        FileStorageService s = new FileStorageService(folder.toString());
        String png = s.simpanFotoKtm(new MockMultipartFile("fotoKtm", "a.png", "image/png", PNG));
        String jpg = s.simpanFotoKtm(new MockMultipartFile("fotoKtm", "b.jpg", "image/jpeg", JPG));

        assertThat(png).startsWith("ktm/").endsWith(".png");
        assertThat(jpg).startsWith("ktm/").endsWith(".jpg");
        assertThat(Files.exists(folder.resolve(png))).isTrue();
        assertThat(s.muat(png)).isNotNull();
    }

    @Test
    void menolakFileBukanGambarWalauNamanyaJpg() {
        FileStorageService s = new FileStorageService(folder.toString());
        MockMultipartFile palsu = new MockMultipartFile("fotoKtm", "x.jpg", "image/jpeg",
                "<script>alert(1)</script>".getBytes());
        assertThatThrownBy(() -> s.simpanFotoKtm(palsu))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("PNG atau JPG");
    }

    @Test
    void menolakFileLebihDari5Mb() {
        FileStorageService s = new FileStorageService(folder.toString());
        byte[] besar = new byte[(int) FileStorageService.MAKS_BYTE + 1];
        System.arraycopy(PNG, 0, besar, 0, PNG.length);
        assertThatThrownBy(() -> s.simpanFotoKtm(new MockMultipartFile("fotoKtm", "b.png", "image/png", besar)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("5 MB");
    }

    @Test
    void fotoBarangOpsionalDanMengembalikanUrlPublik() {
        FileStorageService s = new FileStorageService(folder.toString());
        assertThat(s.simpanFotoBarang(null)).isNull();
        String url = s.simpanFotoBarang(new MockMultipartFile("foto", "a.png", "image/png", PNG));
        assertThat(url).startsWith("/uploads/barang/").endsWith(".png");
    }

    @Test
    void tidakBisaMembukaFileDiLuarFolderUpload() {
        FileStorageService s = new FileStorageService(folder.toString());
        assertThat(s.muat("../../etc/passwd")).isNull();
        assertThat(s.muat("")).isNull();
        assertThat(s.muat("ktm/tidak-ada.png")).isNull();
    }

    @Test
    void hapusMembersihkanFile() {
        FileStorageService s = new FileStorageService(folder.toString());
        String ktm = s.simpanFotoKtm(new MockMultipartFile("fotoKtm", "a.png", "image/png", PNG));
        s.hapus(ktm);
        assertThat(Files.exists(folder.resolve(ktm))).isFalse();
    }

    @Test
    void fotoBuktiOpsionalDanDisimpanDiFolderBukti() {
        FileStorageService s = new FileStorageService(folder.toString());
        assertThat(s.simpanFotoBukti(null)).isNull();
        String path = s.simpanFotoBukti(new MockMultipartFile("fotoBukti", "a.jpg", "image/jpeg", JPG));
        assertThat(path).startsWith("bukti/").endsWith(".jpg");
        assertThat(s.muat(path)).isNotNull();
    }
}
