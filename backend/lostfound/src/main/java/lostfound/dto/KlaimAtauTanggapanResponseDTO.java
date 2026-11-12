package lostfound.dto;

import java.time.LocalDateTime;

public class KlaimAtauTanggapanResponseDTO {

    private Long idInteraksi;
    private Long idKehilangan;
    private Long idPenemu;

    private Integer idPenggunaMerespons;
    private String namaPenggunaMerespons;

    private String catatanBukti;
    private String fotoKtm;
    private String alasanPenolakan;
    private String statusInteraksi;

    private LocalDateTime waktuDibuat;
    private LocalDateTime waktuVerifikasi;

    public Long getIdInteraksi() {
        return idInteraksi;
    }

    public void setIdInteraksi(Long idInteraksi) {
        this.idInteraksi = idInteraksi;
    }

    public Long getIdKehilangan() {
        return idKehilangan;
    }

    public void setIdKehilangan(Long idKehilangan) {
        this.idKehilangan = idKehilangan;
    }

    public Long getIdPenemu() {
        return idPenemu;
    }

    public void setIdPenemu(Long idPenemu) {
        this.idPenemu = idPenemu;
    }

    public Integer getIdPenggunaMerespons() {
        return idPenggunaMerespons;
    }

    public void setIdPenggunaMerespons(Integer idPenggunaMerespons) {
        this.idPenggunaMerespons = idPenggunaMerespons;
    }

    public String getNamaPenggunaMerespons() {
        return namaPenggunaMerespons;
    }

    public void setNamaPenggunaMerespons(String namaPenggunaMerespons) {
        this.namaPenggunaMerespons = namaPenggunaMerespons;
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

    public String getStatusInteraksi() {
        return statusInteraksi;
    }

    public void setStatusInteraksi(String statusInteraksi) {
        this.statusInteraksi = statusInteraksi;
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