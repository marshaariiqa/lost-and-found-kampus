package lostfound.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pengguna")
public class Pengguna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengguna")
    private Integer idPengguna;

    @Column(name = "nama_lengkap", nullable = false, length = 150)
    private String namaLengkap;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "kata_sandi", length = 255)
    private String kataSandi;

    @Column(name = "nomor_telepon", length = 20)
    private String nomorTelepon;

    @Enumerated(EnumType.STRING)
    @Column(name = "peran", nullable = false, columnDefinition = "ENUM('MAHASISWA','ADMIN')")
    private PeranPengguna peran = PeranPengguna.MAHASISWA;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, columnDefinition = "ENUM('LOCAL','GOOGLE')")
    private ProviderLogin provider = ProviderLogin.LOCAL;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    public Pengguna() {
    }

    public Integer getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(Integer idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKataSandi() {
        return kataSandi;
    }

    public void setKataSandi(String kataSandi) {
        this.kataSandi = kataSandi;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public PeranPengguna getPeran() {
        return peran;
    }

    public void setPeran(PeranPengguna peran) {
        this.peran = peran;
    }

    public ProviderLogin getProvider() {
        return provider;
    }

    public void setProvider(ProviderLogin provider) {
        this.provider = provider;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}