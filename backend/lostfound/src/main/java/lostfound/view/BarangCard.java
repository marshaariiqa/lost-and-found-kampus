package lostfound.view;

import java.time.LocalDateTime;

/**
 * Representasi ringkas laporan barang (hilang/temuan) untuk kartu & tabel.
 * Tidak memuat data rahasia.
 */
public record BarangCard(
        String jenis,          // HILANG atau TEMUAN
        Long id,
        String kode,
        String nama,
        String kategori,
        String lokasi,
        String tanggal,
        String status,         // TERSEDIA, SELESAI, DIARSIPKAN
        String statusLabel,
        String urlGambar,
        boolean milikSaya,
        String pelapor,
        LocalDateTime dibuat,
        String alur,        // avail / wait / ok / done / no / arch (status tampilan pemilik)
        String alurLabel,
        String alurHint) {

    public BarangCard conAlur(String kode, String label, String hint) {
        return new BarangCard(jenis, id, this.kode, nama, kategori, lokasi, tanggal, status,
                statusLabel, urlGambar, milikSaya, pelapor, dibuat, kode, label, hint);
    }

    public boolean hilang() {
        return "HILANG".equals(jenis);
    }

    public String jenisPath() {
        return hilang() ? "hilang" : "temuan";
    }

    public String jenisLabel() {
        return hilang() ? "Lost" : "Found";
    }

    public boolean tersedia() {
        return "TERSEDIA".equals(status);
    }
}
