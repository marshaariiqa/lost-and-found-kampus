package lostfound.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class KehilanganBarangDTO {

    @NotNull(message = "Kategori barang wajib dipilih")
    private Integer idKategori;

    @NotBlank(message = "Nama barang wajib diisi")
    @Size(max = 150, message = "Nama barang maksimal 150 karakter")
    private String namaBarang;
    
    @NotBlank(message = "Deskripsi publik wajib diisi")
    private String deskripsiPublik;
    private String spesifikasiRahasia;
    
    @NotBlank(message = "Lokasi kejadian wajib diisi")
    private String lokasiKejadian;

    @NotNull(message = "Tanggal kejadian wajib diisi")
    private LocalDateTime tanggalKejadian;
    
    private String urlGambar;

    public Integer getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(Integer idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getDeskripsiPublik() {
        return deskripsiPublik;
    }

    public void setDeskripsiPublik(String deskripsiPublik) {
        this.deskripsiPublik = deskripsiPublik;
    }

    public String getSpesifikasiRahasia() {
        return spesifikasiRahasia;
    }

    public void setSpesifikasiRahasia(String spesifikasiRahasia) {
        this.spesifikasiRahasia = spesifikasiRahasia;
    }

    public String getLokasiKejadian() {
        return lokasiKejadian;
    }

    public void setLokasiKejadian(String lokasiKejadian) {
        this.lokasiKejadian = lokasiKejadian;
    }

    public LocalDateTime getTanggalKejadian() {
        return tanggalKejadian;
    }

    public void setTanggalKejadian(LocalDateTime tanggalKejadian) {
        this.tanggalKejadian = tanggalKejadian;
    }

    public String getUrlGambar() {
        return urlGambar;
    }

    public void setUrlGambar(String urlGambar) {
        this.urlGambar = urlGambar;
    }
}
