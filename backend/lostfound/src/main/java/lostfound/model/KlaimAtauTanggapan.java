package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "klaim_atau_tanggapan")
public class KlaimAtauTanggapan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaksi")
    private Long idInteraksi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kehilangan")
    private KehilanganBarang kehilanganBarang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_penemu")
    private PenemuBarang penemuBarang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_merespons", nullable = false)
    private Pengguna penggunaMerespons;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_verifikator")
    private Pengguna adminVerifikator;

    @Column(name = "catatan_bukti", columnDefinition = "TEXT")
    private String catatanBukti;

    @Column(name = "foto_ktm")
    private String fotoKtm;

    @Column(name = "alasan_penolakan", columnDefinition = "TEXT")
    private String alasanPenolakan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_interaksi", nullable = false)
    private StatusInteraksi statusInteraksi = StatusInteraksi.MENUNGGU_VERIFIKASI;

    @Column(name = "tenggat_tanggapan")
    private LocalDateTime tenggatTanggapan;

    @Column(name = "waktu_dibuat", insertable = false, updatable = false)
    private LocalDateTime waktuDibuat;

    @Column(name = "waktu_verifikasi")
    private LocalDateTime waktuVerifikasi;

    public KlaimAtauTanggapan() {
    }

    // Getter dan Setter

    public Long getIdInteraksi() {
        return idInteraksi;
    }

    public void setIdInteraksi(Long idInteraksi) {
        this.idInteraksi = idInteraksi;
    }

    public KehilanganBarang getKehilanganBarang() {
        return kehilanganBarang;
    }

    public void setKehilanganBarang(KehilanganBarang kehilanganBarang) {
        this.kehilanganBarang = kehilanganBarang;
    }

    public PenemuBarang getPenemuBarang() {
        return penemuBarang;
    }

    public void setPenemuBarang(PenemuBarang penemuBarang) {
        this.penemuBarang = penemuBarang;
    }

    public Pengguna getPenggunaMerespons() {
        return penggunaMerespons;
    }

    public void setPenggunaMerespons(Pengguna penggunaMerespons) {
        this.penggunaMerespons = penggunaMerespons;
    }

    public Pengguna getAdminVerifikator() {
        return adminVerifikator;
    }

    public void setAdminVerifikator(Pengguna adminVerifikator) {
        this.adminVerifikator = adminVerifikator;
    }

    public String getCatatanBukti() {
        return catatanBukti;
    }

    public void setCatatanBukti(String catatanBukti) {
        this.catatanBukti = catatanBukti;
    }

    public String getFotoKtm() {
        return fotoKtm;
    }

    public void setFotoKtm(String fotoKtm) {
        this.fotoKtm = fotoKtm;
    }

    public String getAlasanPenolakan() {
        return alasanPenolakan;
    }

    public void setAlasanPenolakan(String alasanPenolakan) {
        this.alasanPenolakan = alasanPenolakan;
    }

    public StatusInteraksi getStatusInteraksi() {
        return statusInteraksi;
    }

    public void setStatusInteraksi(StatusInteraksi statusInteraksi) {
        this.statusInteraksi = statusInteraksi;
    }

    public LocalDateTime getTenggatTanggapan() {
        return tenggatTanggapan;
    }

    public void setTenggatTanggapan(LocalDateTime tenggatTanggapan) {
        this.tenggatTanggapan = tenggatTanggapan;
    }

    public LocalDateTime getWaktuDibuat() {
        return waktuDibuat;
    }

    public void setWaktuDibuat(LocalDateTime waktuDibuat) {
        this.waktuDibuat = waktuDibuat;
    }

    public LocalDateTime getWaktuVerifikasi() {
        return waktuVerifikasi;
    }

    public void setWaktuVerifikasi(LocalDateTime waktuVerifikasi) {
        this.waktuVerifikasi = waktuVerifikasi;
    }
}