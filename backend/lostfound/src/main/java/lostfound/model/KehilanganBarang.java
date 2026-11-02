package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kehilangan_barang")
public class KehilanganBarang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kehilangan")
    private Long idKehilangan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kategori", nullable = false)
    private Kategori kategori;

    @Column(name = "nama_barang", nullable = false, length = 150)
    private String namaBarang;

    @Column(name = "deskripsi_publik", columnDefinition = "TEXT")
    private String deskripsiPublik;

    @Column(name = "spesifikasi_rahasia", columnDefinition = "TEXT")
    private String spesifikasiRahasia;

    @Column(name = "lokasi_kejadian", nullable = false)
    private String lokasiKejadian;

    @Column(name = "tanggal_kejadian", nullable = false)
    private LocalDateTime tanggalKejadian;

    @Column(name = "url_gambar")
    private String urlGambar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusBarang status = StatusBarang.TERSEDIA;

    public KehilanganBarang() {
    }

    // Getter dan Setter
    public Long getIdKehilangan() {
        return idKehilangan;
    }

    public void setIdKehilangan(Long idKehilangan) {
        this.idKehilangan = idKehilangan;
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

    public StatusBarang getStatus() {
        return status;
    }

    public void setStatus(StatusBarang status) {
        this.status = status;
    }
}