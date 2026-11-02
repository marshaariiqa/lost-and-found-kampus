package lostfound.dto;

import java.time.LocalDateTime;

public class PenemuBarangDTO {

    private Integer idKategori;
    private String namaBarang;
    private String deskripsi;
    private String lokasiDitemukan;
    private LocalDateTime tanggalDitemukan;
    private String urlGambar;
    private String lokasiPenyimpanan;

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

    public String getLokasiPenyimpanan() {
        return lokasiPenyimpanan;
    }

    public void setLokasiPenyimpanan(String lokasiPenyimpanan) {
        this.lokasiPenyimpanan = lokasiPenyimpanan;
    }
}
