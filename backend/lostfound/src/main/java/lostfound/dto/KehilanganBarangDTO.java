package lostfound.dto;

import java.time.LocalDateTime;

public class KehilanganBarangDTO {

    private Integer idKategori;
    private String namaBarang;
    private String deskripsiPublik;
    private String spesifikasiRahasia;
    private String lokasiKejadian;
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
