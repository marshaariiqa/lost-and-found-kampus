package lostfound.service;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.KehilanganBarangResponseDTO;
import lostfound.model.KehilanganBarang;
import lostfound.model.Pengguna;
import lostfound.model.Kategori;
import lostfound.model.StatusBarang;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.PenggunaRepository;
import lostfound.repository.KategoriRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class KehilanganBarangService {

    @Autowired
    private KehilanganBarangRepository kehilanganBarangRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    public List<KehilanganBarang> getAllKehilangan() {
        return kehilanganBarangRepository.findAll()
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public KehilanganBarang restoreKehilangan(Long id) {

        KehilanganBarang barang = kehilanganBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan kehilangan tidak ditemukan"));

        if (barang.getStatus() != StatusBarang.DIARSIPKAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Laporan ini tidak berada dalam arsip");
        }

        barang.setStatus(StatusBarang.TERSEDIA);
        barang.setWaktuDibuat(java.time.LocalDateTime.now());

        return kehilanganBarangRepository.save(barang);
    }

    public List<KehilanganBarang> cariKehilangan(String namaBarang) {
        return kehilanganBarangRepository
                .findByNamaBarangContainingIgnoreCase(namaBarang)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public List<KehilanganBarang> filterKehilanganByKategori(Integer idKategori) {
        return kehilanganBarangRepository
                .findByKategori_IdKategori(idKategori)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public Optional<KehilanganBarang> getKehilanganById(Long id) {
        return kehilanganBarangRepository.findById(id);
    }

    public KehilanganBarang createKehilangan(
            KehilanganBarangDTO dto, String email) {

        Pengguna pengguna = penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        Kategori kategori = kategoriRepository.findById(dto.getIdKategori())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Kategori barang tidak ditemukan"));

        KehilanganBarang barang = new KehilanganBarang();

        barang.setPengguna(pengguna);
        barang.setKategori(kategori);
        barang.setNamaBarang(dto.getNamaBarang());
        barang.setDeskripsiPublik(dto.getDeskripsiPublik());
        barang.setSpesifikasiRahasia(dto.getSpesifikasiRahasia());
        barang.setLokasiKejadian(dto.getLokasiKejadian());
        barang.setTanggalKejadian(dto.getTanggalKejadian());
        barang.setUrlGambar(dto.getUrlGambar());

        return kehilanganBarangRepository.save(barang);
    }

    public KehilanganBarang updateKehilangan(
            Long id, KehilanganBarangDTO dto, String email) {

        KehilanganBarang barang = kehilanganBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Laporan kehilangan tidak ditemukan"));

        if (!barang.getPengguna().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kamu tidak memiliki akses untuk mengubah laporan ini");
        }

        Kategori kategori = kategoriRepository.findById(dto.getIdKategori())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Kategori tidak ditemukan"));

        barang.setKategori(kategori);
        barang.setNamaBarang(dto.getNamaBarang());
        barang.setDeskripsiPublik(dto.getDeskripsiPublik());
        barang.setSpesifikasiRahasia(dto.getSpesifikasiRahasia());
        barang.setLokasiKejadian(dto.getLokasiKejadian());
        barang.setTanggalKejadian(dto.getTanggalKejadian());
        barang.setUrlGambar(dto.getUrlGambar());

        return kehilanganBarangRepository.save(barang);
    }

    public void deleteKehilangan(Long id, String email) {

        KehilanganBarang barang = kehilanganBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Laporan kehilangan tidak ditemukan"));

        if (!barang.getPengguna().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kamu tidak memiliki akses untuk menghapus laporan ini");
        }

        kehilanganBarangRepository.delete(barang);
    }

    public KehilanganBarangResponseDTO toResponseDTO(KehilanganBarang barang) {

        KehilanganBarangResponseDTO dto = new KehilanganBarangResponseDTO();

        dto.setIdKehilangan(barang.getIdKehilangan());
        dto.setNamaBarang(barang.getNamaBarang());
        dto.setDeskripsiPublik(barang.getDeskripsiPublik());
        dto.setLokasiKejadian(barang.getLokasiKejadian());
        dto.setTanggalKejadian(barang.getTanggalKejadian());
        dto.setUrlGambar(barang.getUrlGambar());
        dto.setStatus(barang.getStatus().name());

        dto.setIdKategori(barang.getKategori().getIdKategori());
        dto.setNamaKategori(barang.getKategori().getNamaKategori());

        dto.setIdPengguna(barang.getPengguna().getIdPengguna());
        dto.setNamaPelapor(barang.getPengguna().getNamaLengkap());

        return dto;
    }

    public List<KehilanganBarang> getArsipKehilangan() {
        return kehilanganBarangRepository.findByStatus(StatusBarang.DIARSIPKAN);
    }
}