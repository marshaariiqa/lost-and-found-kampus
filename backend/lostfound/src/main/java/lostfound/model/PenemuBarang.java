package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "penemu_barang")
public class PenemuBarang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penemu")
    private Long idPenemu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kategori", nullable = false)
    private Kategori kategori;

    @Column(name = "nama_barang", nullable = false, length = 150)
    private String namaBarang;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "lokasi_ditemukan", nullable = false)
    private String lokasiDitemukan;

    @Column(name = "tanggal_ditemukan", nullable = false)
    private LocalDateTime tanggalDitemukan;

    @Column(name = "url_gambar")
    private String urlGambar;

    @Column(name = "lokasi_penyimpanan")
    private String lokasiPenyimpanan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusBarang status = StatusBarang.TERSEDIA;

    public PenemuBarang() {
    }

    // Getter dan Setter
    public Long getIdPenemu() {
        return idPenemu;
    }

    public void setIdPenemu(Long idPenemu) {
        this.idPenemu = idPenemu;
    }

    public Pengguna getPengguna() {
        return pengguna;
    }

    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
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

    public StatusBarang getStatus() {
        return status;
    }

    public void setStatus(StatusBarang status) {
        this.status = status;
    }
}   