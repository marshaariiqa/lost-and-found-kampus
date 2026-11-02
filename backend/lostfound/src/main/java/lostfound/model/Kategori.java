package lostfound.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kategori")
public class Kategori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kategori")
    private Integer idKategori;

    @Column(name = "nama_kategori", nullable = false, unique = true, length = 100)
    private String namaKategori;

    public Kategori() {
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
}