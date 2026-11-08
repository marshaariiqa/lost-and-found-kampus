package lostfound.dto;

import java.time.LocalDateTime;

public class KehilanganBarangResponseDTO {

    private Long idKehilangan;
    private String namaBarang;
    private String deskripsiPublik;
    private String lokasiKejadian;
    private LocalDateTime tanggalKejadian;
    private String urlGambar;
    private String status;

    private Integer idKategori;
    private String namaKategori;

    private Integer idPengguna;
    private String namaPelapor;

    public Long getIdKehilangan() {
        return idKehilangan;
    }

    public void setIdKehilangan(Long idKehilangan) {
        this.idKehilangan = idKehilangan;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(Integer idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public Integer getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(Integer idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaPelapor() {
        return namaPelapor;
    }

    public void setNamaPelapor(String namaPelapor) {
        this.namaPelapor = namaPelapor;
    }
}
