package lostfound.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PenemuBarangDTO {

    @NotNull(message = "Kategori barang wajib dipilih")
    private Integer idKategori;

    @NotBlank(message = "Nama barang wajib diisi")
    @Size(max = 150, message = "Nama barang maksimal 150 karakter")
    private String namaBarang;

    private String deskripsi;

    @NotBlank(message = "Lokasi ditemukan wajib diisi")
    private String lokasiDitemukan;

    @NotNull(message = "Tanggal ditemukan wajib diisi")
    private LocalDateTime tanggalDitemukan;

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

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getLokasiDitemukan() {
        return lokasiDitemukan;
    }

    public void setLokasiDitemukan(String lokasiDitemukan) {
        this.lokasiDitemukan = lokasiDitemukan;
    }

    public LocalDateTime getTanggalDitemukan() {
        return tanggalDitemukan;
    }

    public void setTanggalDitemukan(LocalDateTime tanggalDitemukan) {
        this.tanggalDitemukan = tanggalDitemukan;
    }

    public String getUrlGambar() {
        return urlGambar;
    }

    public void setUrlGambar(String urlGambar) {
        this.urlGambar = urlGambar;
    }
}
