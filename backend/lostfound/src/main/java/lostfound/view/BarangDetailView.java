package lostfound.view;

/**
 * Detail laporan untuk halaman detail. spesifikasiRahasia hanya terisi
 * bila yang melihat adalah pemilik laporan.
 */
public record BarangDetailView(
        String jenis,
        Long id,
        String kode,
        String nama,
        String kategori,
        String deskripsi,
        String lokasi,
        String tanggal,
        String status,
        String statusLabel,
        String urlGambar,
        String pelapor,
        String lokasiPenyimpanan,
        String spesifikasiRahasia,
        boolean milikSaya,
        boolean bisaKlaim,
        Long idInteraksiSaya,
        String statusInteraksiSaya,
        long pengajuanMenunggu,
        boolean diPosko,         // untuk laporan temuan: sudah diterima di Posko
        String judulArsip,       // hanya terisi bila laporan DIARSIPKAN
        String pesanArsip) {

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
