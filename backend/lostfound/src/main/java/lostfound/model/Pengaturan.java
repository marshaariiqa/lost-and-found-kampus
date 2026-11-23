package lostfound.model;

import jakarta.persistence.*;

/**
 * Pengaturan sistem berbentuk key-value (tabel pengaturan_sistem).
 */
@Entity
@Table(name = "pengaturan_sistem")
public class Pengaturan {

    @Id
    @Column(name = "kunci", length = 100)
    private String kunci;

    @Column(name = "nilai", nullable = false, length = 500)
    private String nilai;

    public Pengaturan() {
    }

    public Pengaturan(String kunci, String nilai) {
        this.kunci = kunci;
        this.nilai = nilai;
    }

    public String getKunci() {
        return kunci;
    }

    public void setKunci(String kunci) {
        this.kunci = kunci;
    }

    public String getNilai() {
        return nilai;
    }

    public void setNilai(String nilai) {
        this.nilai = nilai;
    }
}
