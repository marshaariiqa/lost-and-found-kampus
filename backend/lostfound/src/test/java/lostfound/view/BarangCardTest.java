package lostfound.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class BarangCardTest {

    @Test
    void conAlurMengubahStatusTampilanTanpaMengubahDataLain() {
        BarangCard kartu = new BarangCard("TEMUAN", 4L, "#TMN-00004", "Dompet", "Dompet & Aksesoris",
                "Selasar", "21 Nov 2026", "TERSEDIA", "Tersedia", null, true, "Marsha K.",
                LocalDateTime.now(), "avail", "Tersedia", "");

        BarangCard menunggu = kartu.conAlur("wait", "Menunggu Verifikasi", "1 pengajuan sedang diperiksa admin");

        assertThat(menunggu.alur()).isEqualTo("wait");
        assertThat(menunggu.alurLabel()).isEqualTo("Menunggu Verifikasi");
        // status asli laporan tidak berubah
        assertThat(menunggu.status()).isEqualTo("TERSEDIA");
        assertThat(menunggu.kode()).isEqualTo("#TMN-00004");
        assertThat(menunggu.nama()).isEqualTo("Dompet");
        assertThat(menunggu.jenisPath()).isEqualTo("temuan");
    }
}
