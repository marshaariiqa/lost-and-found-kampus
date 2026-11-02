package lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_aktivitas")
public class LogAktivitas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kehilangan")
    private KehilanganBarang kehilanganBarang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_penemu")
    private PenemuBarang penemuBarang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin", nullable = false)
    private Pengguna admin;

    @Column(name = "tindakan", nullable = false)
    private String tindakan;

    @Column(name = "waktu_catat", insertable = false, updatable = false)
    private LocalDateTime waktuCatat;

    public LogAktivitas() {
    }

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public KehilanganBarang getKehilanganBarang() { return kehilanganBarang; }
    public void setKehilanganBarang(KehilanganBarang kehilanganBarang) {
        this.kehilanganBarang = kehilanganBarang;
    }

    public PenemuBarang getPenemuBarang() { return penemuBarang; }
    public void setPenemuBarang(PenemuBarang penemuBarang) {
        this.penemuBarang = penemuBarang;
    }

    public Pengguna getAdmin() { return admin; }
    public void setAdmin(Pengguna admin) { this.admin = admin; }

    public String getTindakan() { return tindakan; }
    public void setTindakan(String tindakan) { this.tindakan = tindakan; }

    public LocalDateTime getWaktuCatat() { return waktuCatat; }
    public void setWaktuCatat(LocalDateTime waktuCatat) {
        this.waktuCatat = waktuCatat;
    }
}