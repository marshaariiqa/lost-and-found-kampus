package lostfound.service;

import lostfound.dto.PenemuBarangDTO;
import lostfound.dto.PenemuBarangResponseDTO;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.Kategori;
import lostfound.model.StatusBarang;
import lostfound.repository.PenemuBarangRepository;
import lostfound.repository.PenggunaRepository;
import lostfound.repository.KategoriRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class PenemuBarangService {

    @Autowired
    private PenemuBarangRepository penemuBarangRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    public List<PenemuBarang> getAllTemuan() {
        return penemuBarangRepository.findAll()
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public PenemuBarang restoreTemuan(Long id) {

        PenemuBarang barang = penemuBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan barang temuan tidak ditemukan"));

        if (barang.getStatus() != StatusBarang.DIARSIPKAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Laporan ini tidak berada dalam arsip");
        }

        barang.setStatus(StatusBarang.TERSEDIA);
        barang.setWaktuDibuat(java.time.LocalDateTime.now());

        return penemuBarangRepository.save(barang);
    }

    public List<PenemuBarang> cariTemuan(String namaBarang) {
        return penemuBarangRepository
                .findByNamaBarangContainingIgnoreCase(namaBarang)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public List<PenemuBarang> filterTemuanByKategori(Integer idKategori) {
        return penemuBarangRepository
                .findByKategori_IdKategori(idKategori)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    public Optional<PenemuBarang> getTemuanById(Long id) {
        return penemuBarangRepository.findById(id);
    }

    public PenemuBarang createTemuan(
            PenemuBarangDTO dto, String email) {

        Pengguna pengguna = penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        Kategori kategori = kategoriRepository.findById(dto.getIdKategori())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Kategori barang tidak ditemukan"));

        PenemuBarang barang = new PenemuBarang();

        barang.setPengguna(pengguna);
        barang.setKategori(kategori);
        barang.setNamaBarang(dto.getNamaBarang());
        barang.setDeskripsi(dto.getDeskripsi());
        barang.setLokasiDitemukan(dto.getLokasiDitemukan());
        barang.setTanggalDitemukan(dto.getTanggalDitemukan());
        barang.setUrlGambar(dto.getUrlGambar());
        barang.setLokasiPenyimpanan("Posko FIK");

        return penemuBarangRepository.save(barang);
    }

    public PenemuBarang updateTemuan(
            Long id, PenemuBarangDTO dto, String email) {

        PenemuBarang barang = penemuBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Laporan barang temuan tidak ditemukan"));

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
        barang.setDeskripsi(dto.getDeskripsi());
        barang.setLokasiDitemukan(dto.getLokasiDitemukan());
        barang.setTanggalDitemukan(dto.getTanggalDitemukan());
        barang.setUrlGambar(dto.getUrlGambar());

        return penemuBarangRepository.save(barang);
    }

    public void deleteTemuan(Long id, String email) {

        PenemuBarang barang = penemuBarangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Laporan barang temuan tidak ditemukan"));

        if (!barang.getPengguna().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kamu tidak memiliki akses untuk menghapus laporan ini");
        }

        penemuBarangRepository.delete(barang);
    }

    public PenemuBarangResponseDTO toResponseDTO(PenemuBarang barang) {

        PenemuBarangResponseDTO dto = new PenemuBarangResponseDTO();

        dto.setIdPenemu(barang.getIdPenemu());
        dto.setNamaBarang(barang.getNamaBarang());
        dto.setDeskripsi(barang.getDeskripsi());
        dto.setLokasiDitemukan(barang.getLokasiDitemukan());
        dto.setLokasiPenyimpanan(barang.getLokasiPenyimpanan());
        dto.setTanggalDitemukan(barang.getTanggalDitemukan());
        dto.setUrlGambar(barang.getUrlGambar());
        dto.setStatus(barang.getStatus().name());

        dto.setIdKategori(barang.getKategori().getIdKategori());
        dto.setNamaKategori(barang.getKategori().getNamaKategori());

        dto.setIdPengguna(barang.getPengguna().getIdPengguna());
        dto.setNamaPelapor(barang.getPengguna().getNamaLengkap());

        return dto;
    }

    public List<PenemuBarang> getArsipTemuan() {
        return penemuBarangRepository.findByStatus(StatusBarang.DIARSIPKAN);
    }
}