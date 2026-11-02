package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifikasi")
public class Notifikasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notifikasi")
    private Long idNotifikasi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_interaksi")
    private KlaimAtauTanggapan interaksi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kehilangan")
    private KehilanganBarang kehilanganBarang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_penemu")
    private PenemuBarang penemuBarang;

    @Column(name = "judul", nullable = false, length = 150)
    private String judul;

    @Column(name = "pesan", nullable = false, columnDefinition = "TEXT")
    private String pesan;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_notifikasi", nullable = false)
    private JenisNotifikasi jenisNotifikasi;

    @Column(name = "status_dibaca", nullable = false)
    private boolean statusDibaca = false;

    @Column(name = "waktu_dibuat", insertable = false, updatable = false)
    private LocalDateTime waktuDibuat;

    public Notifikasi() {
    }

    public Long getIdNotifikasi() { return idNotifikasi; }
    public void setIdNotifikasi(Long idNotifikasi) {
        this.idNotifikasi = idNotifikasi;
    }

    public Pengguna getPengguna() { return pengguna; }
    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public KlaimAtauTanggapan getInteraksi() { return interaksi; }
    public void setInteraksi(KlaimAtauTanggapan interaksi) {
        this.interaksi = interaksi;
    }

    public KehilanganBarang getKehilanganBarang() { return kehilanganBarang; }
    public void setKehilanganBarang(KehilanganBarang kehilanganBarang) {
        this.kehilanganBarang = kehilanganBarang;
    }

    public PenemuBarang getPenemuBarang() { return penemuBarang; }
    public void setPenemuBarang(PenemuBarang penemuBarang) {
        this.penemuBarang = penemuBarang;
    }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getPesan() { return pesan; }
    public void setPesan(String pesan) { this.pesan = pesan; }

    public JenisNotifikasi getJenisNotifikasi() { return jenisNotifikasi; }
    public void setJenisNotifikasi(JenisNotifikasi jenisNotifikasi) {
        this.jenisNotifikasi = jenisNotifikasi;
    }

    public boolean isStatusDibaca() { return statusDibaca; }
    public void setStatusDibaca(boolean statusDibaca) {
        this.statusDibaca = statusDibaca;
    }

    public LocalDateTime getWaktuDibuat() { return waktuDibuat; }
    public void setWaktuDibuat(LocalDateTime waktuDibuat) {
        this.waktuDibuat = waktuDibuat;
    }
}