package lostfound.service;

import lostfound.dto.PenemuBarangDTO;
import lostfound.dto.PenemuBarangResponseDTO;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.model.Kategori;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.repository.PenemuBarangRepository;
import lostfound.repository.PenggunaRepository;
import lostfound.repository.KategoriRepository;
import lostfound.repository.KlaimAtauTanggapanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
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

    @Autowired
    private KlaimAtauTanggapanRepository klaimAtauTanggapanRepository;

    // Mengambil semua laporan temuan yang tersedia
    @Transactional(readOnly = true)
    public List<PenemuBarang> getAllTemuan() {
        return penemuBarangRepository.findByStatusAndDihapusFalse(
                StatusBarang.TERSEDIA
        );
    }

    // Mengembalikan laporan dari arsip
    @Transactional
    public PenemuBarang restoreTemuan(Long id) {
        PenemuBarang barang = penemuBarangRepository
                .findByIdPenemuAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan barang temuan tidak ditemukan"
                ));

        if (barang.getStatus() != StatusBarang.DIARSIPKAN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan ini tidak berada dalam arsip"
            );
        }

        barang.setStatus(StatusBarang.TERSEDIA);
        barang.setWaktuDibuat(LocalDateTime.now());

        return penemuBarangRepository.save(barang);
    }

    // Mencari laporan berdasarkan nama barang
    @Transactional(readOnly = true)
    public List<PenemuBarang> cariTemuan(String namaBarang) {
        return penemuBarangRepository
                .findByNamaBarangContainingIgnoreCaseAndDihapusFalse(
                        namaBarang
                )
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    // Filter laporan berdasarkan kategori
    @Transactional(readOnly = true)
    public List<PenemuBarang> filterTemuanByKategori(Integer idKategori) {
        return penemuBarangRepository
                .findByKategori_IdKategoriAndDihapusFalse(idKategori)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    // Mengambil detail laporan yang belum dihapus
    @Transactional(readOnly = true)
    public Optional<PenemuBarang> getTemuanById(Long id) {
        return penemuBarangRepository.findByIdPenemuAndDihapusFalse(id);
    }

    // Membuat laporan barang temuan
    @Transactional
    public PenemuBarang createTemuan(
            PenemuBarangDTO dto,
            String email
    ) {
        Pengguna pengguna = penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pengguna tidak ditemukan"
                ));

        Kategori kategori = kategoriRepository.findById(dto.getIdKategori())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Kategori barang tidak ditemukan"
                ));

        PenemuBarang barang = new PenemuBarang();

        barang.setPengguna(pengguna);
        barang.setKategori(kategori);
        barang.setNamaBarang(dto.getNamaBarang());
        barang.setDeskripsi(dto.getDeskripsi());
        barang.setLokasiDitemukan(dto.getLokasiDitemukan());
        barang.setTanggalDitemukan(dto.getTanggalDitemukan());
        barang.setUrlGambar(dto.getUrlGambar());
        String lokasiSimpan = dto.getLokasiPenyimpanan();
        barang.setLokasiPenyimpanan(
                lokasiSimpan == null || lokasiSimpan.isBlank() ? "Posko FIK" : lokasiSimpan.trim());

        return penemuBarangRepository.save(barang);
    }

    // Memeriksa apakah laporan memiliki interaksi aktif
    private boolean memilikiInteraksiAktif(Long id) {
        return klaimAtauTanggapanRepository
                .existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                        id,
                        StatusInteraksi.MENUNGGU_VERIFIKASI
                )
                || klaimAtauTanggapanRepository
                .existsByPenemuBarang_IdPenemuAndStatusInteraksi(
                        id,
                        StatusInteraksi.DISETUJUI
                );
    }

    // Mengubah laporan barang temuan
    @Transactional
    public PenemuBarang updateTemuan(
            Long id,
            PenemuBarangDTO dto,
            String email
    ) {
        PenemuBarang barang = penemuBarangRepository
                .findByIdPenemuAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan barang temuan tidak ditemukan"
                ));

        if (!barang.getPengguna().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kamu tidak memiliki akses untuk mengubah laporan ini"
            );
        }

        if (barang.getStatus() != StatusBarang.TERSEDIA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan hanya dapat diubah jika masih tersedia"
            );
        }

        if (memilikiInteraksiAktif(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan tidak dapat diubah karena masih memiliki interaksi yang diproses"
            );
        }

        Kategori kategori = kategoriRepository.findById(dto.getIdKategori())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Kategori tidak ditemukan"
                ));

        barang.setKategori(kategori);
        barang.setNamaBarang(dto.getNamaBarang());
        barang.setDeskripsi(dto.getDeskripsi());
        barang.setLokasiDitemukan(dto.getLokasiDitemukan());
        barang.setTanggalDitemukan(dto.getTanggalDitemukan());
        barang.setUrlGambar(dto.getUrlGambar());

        return penemuBarangRepository.save(barang);
    }

    // Soft delete laporan yang masih tersedia (milik sendiri)
    @Transactional
    public void deleteTemuan(Long id, String email) {
        PenemuBarang barang = penemuBarangRepository
                .findByIdPenemuAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan barang temuan tidak ditemukan"
                ));

        if (!barang.getPengguna().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kamu tidak memiliki akses untuk menghapus laporan ini"
            );
        }

        // Laporan SELESAI/DIARSIPKAN disimpan sebagai riwayat dan tidak boleh dihapus pengguna
        if (barang.getStatus() != StatusBarang.TERSEDIA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan hanya dapat dihapus jika masih tersedia"
            );
        }

        if (memilikiInteraksiAktif(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan tidak dapat dihapus karena masih memiliki interaksi yang diproses"
            );
        }

        barang.setDihapus(true);
        penemuBarangRepository.save(barang);
    }

    // Mengubah entity menjadi response DTO
    @Transactional(readOnly = true)
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

    // Mengambil laporan arsip yang belum dihapus
    @Transactional(readOnly = true)
    public List<PenemuBarang> getArsipTemuan() {
        return penemuBarangRepository.findByStatusAndDihapusFalse(
                StatusBarang.DIARSIPKAN
        );
    }
}