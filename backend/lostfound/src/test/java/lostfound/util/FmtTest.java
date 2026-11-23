package lostfound.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class FmtTest {

    @Test
    void formatTanggalIndonesia() {
        assertThat(Fmt.tanggal(LocalDateTime.of(2026, 5, 3, 9, 5))).isEqualTo("3 Mei 2026");
        assertThat(Fmt.tanggalWaktu(LocalDateTime.of(2026, 10, 12, 9, 5))).isEqualTo("12 Okt 2026, 09.05");
        assertThat(Fmt.tanggal(null)).isEqualTo("-");
    }

    @Test
    void kodeDanInisial() {
        assertThat(Fmt.kode("KLM", 21L)).isEqualTo("#KLM-00021");
        assertThat(Fmt.inisial("Aulia Rahma Putri")).isEqualTo("AP");
        assertThat(Fmt.inisial("Budi")).isEqualTo("B");
        assertThat(Fmt.namaSingkat("Aulia Rahma Putri")).isEqualTo("Aulia P.");
    }

    @Test
    void urlGambarHanyaUploadSendiriAtauHttps() {
        assertThat(Fmt.urlGambarAman("/uploads/barang/a.png")).isEqualTo("/uploads/barang/a.png");
        assertThat(Fmt.urlGambarAman("https://contoh.id/a.png")).isEqualTo("https://contoh.id/a.png");
        assertThat(Fmt.urlGambarAman("javascript:alert(1)")).isNull();
        assertThat(Fmt.urlGambarAman("/uploads/barang/../../x")).isNull();
        assertThat(Fmt.urlGambarAman("  ")).isNull();
    }

    @Test
    void kelompokWaktuNotifikasi() {
        assertThat(Fmt.kelompokHari(LocalDateTime.now())).isEqualTo("Hari ini");
        assertThat(Fmt.kelompokHari(LocalDateTime.now().minusDays(1))).isEqualTo("Kemarin");
        assertThat(Fmt.kelompokHari(LocalDateTime.now().minusDays(5))).isEqualTo("Sebelumnya");
    }
}
