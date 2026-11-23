package lostfound.service;

import static org.assertj.core.api.Assertions.assertThat;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.StatusInteraksi;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class ArsipBarangServiceTest {

    private static final LocalDateTime SEKARANG = LocalDateTime.of(2026, 11, 30, 10, 0);

    private static KlaimAtauTanggapan interaksi(StatusInteraksi status, int hariLalu) {
        KlaimAtauTanggapan i = new KlaimAtauTanggapan();
        i.setStatusInteraksi(status);
        i.setWaktuVerifikasi(SEKARANG.minusDays(hariLalu));
        return i;
    }

    @Test
    void tanpaInteraksiDiarsipkanSetelahLebihDari7Hari() {
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(8), null, List.of())).isTrue();
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(6), null, List.of())).isFalse();
    }

    @Test
    void interaksiMenungguVerifikasiTidakPernahDiarsipkan() {
        List<KlaimAtauTanggapan> menunggu = List.of(interaksi(StatusInteraksi.MENUNGGU_VERIFIKASI, 0));
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60), null, menunggu)).isFalse();
    }

    @Test
    void disetujuiTanpaProgresDiarsipkanSetelah7HariDariPersetujuan() {
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60), null,
                List.of(interaksi(StatusInteraksi.DISETUJUI, 8)))).isTrue();
        // laporan sudah lama, tetapi persetujuan baru 2 hari -> masih berjalan
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60), null,
                List.of(interaksi(StatusInteraksi.DISETUJUI, 2)))).isFalse();
    }

    @Test
    void penerimaanDiPoskoSetelahDisetujuiDihitungSebagaiProgres() {
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60),
                SEKARANG.minusDays(1), List.of(interaksi(StatusInteraksi.DISETUJUI, 9)))).isFalse();
    }

    @Test
    void laporanYangDipulihkanMemulaiUlangTimerWalauAdaPersetujuanLama() {
        // waktu_dibuat di-reset saat dipulihkan (1 hari lalu); persetujuan lama 20 hari lalu
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(1), null,
                List.of(interaksi(StatusInteraksi.DISETUJUI, 20)))).isFalse();
        // 8 hari setelah dipulihkan tanpa progres -> boleh diarsipkan lagi
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(8), null,
                List.of(interaksi(StatusInteraksi.DISETUJUI, 20)))).isTrue();
    }

    @Test
    void ditolakMengikutiAturanMasaAktifDariPenolakanTerakhir() {
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60), null,
                List.of(interaksi(StatusInteraksi.DITOLAK, 2)))).isFalse();
        assertThat(ArsipBarangService.perluDiarsipkan(SEKARANG, SEKARANG.minusDays(60), null,
                List.of(interaksi(StatusInteraksi.DITOLAK, 8)))).isTrue();
    }

    @Test
    void alasanArsipDiturunkanDariJenisDanProses() {
        assertThat(ArsipBarangService.alasan(true, false)).isEqualTo("TIDAK DIKLAIM");
        assertThat(ArsipBarangService.alasan(false, false)).isEqualTo("TIDAK DITEMUKAN");
        assertThat(ArsipBarangService.alasan(true, true)).isEqualTo("PROSES TIDAK DILANJUTKAN");
        assertThat(ArsipBarangService.alasan(false, true)).isEqualTo("PROSES TIDAK DILANJUTKAN");
    }

    @Test
    void pesanArsipMengarahkanPemilikKeAdminTanpaTombolPemulihan() {
        assertThat(ArsipBarangService.judul("TIDAK DIKLAIM")).isEqualTo("Laporan Diarsipkan — Tidak Diklaim");
        assertThat(ArsipBarangService.judul("TIDAK DITEMUKAN")).isEqualTo("Laporan Diarsipkan — Tidak Ditemukan");
        assertThat(ArsipBarangService.pesanPemilik("TIDAK DIKLAIM"))
                .contains("tidak ada klaim selama 7 hari").contains("hubungi admin FindUp!");
        assertThat(ArsipBarangService.pesanPemilik("TIDAK DITEMUKAN"))
                .contains("tidak ada tanggapan atau penemuan selama 7 hari").contains("hubungi admin FindUp!");
    }
}
