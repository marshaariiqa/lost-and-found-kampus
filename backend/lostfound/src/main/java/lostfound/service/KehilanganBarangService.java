
package lostfound.service;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.KehilanganBarangResponseDTO;
import lostfound.model.KehilanganBarang;
import lostfound.model.Pengguna;
import lostfound.model.Kategori;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.repository.KehilanganBarangRepository;
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
public class KehilanganBarangService {

    @Autowired
    private KehilanganBarangRepository kehilanganBarangRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private KlaimAtauTanggapanRepository klaimAtauTanggapanRepository;

    // Mengambil laporan yang tersedia dan belum dihapus
    @Transactional(readOnly = true)
    public List<KehilanganBarang> getAllKehilangan() {
        return kehilanganBarangRepository.findByStatusAndDihapusFalse(
                StatusBarang.TERSEDIA
        );
    }

    // Mengembalikan laporan dari arsip
    @Transactional
    public KehilanganBarang restoreKehilangan(Long id) {
        KehilanganBarang barang = kehilanganBarangRepository
                .findByIdKehilanganAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan kehilangan tidak ditemukan"
                ));

        if (barang.getStatus() != StatusBarang.DIARSIPKAN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Laporan ini tidak berada dalam arsip"
            );
        }

        barang.setStatus(StatusBarang.TERSEDIA);
        barang.setWaktuDibuat(LocalDateTime.now());

        return kehilanganBarangRepository.save(barang);
    }

    // Mencari laporan yang tersedia dan belum dihapus
    @Transactional(readOnly = true)
    public List<KehilanganBarang> cariKehilangan(String namaBarang) {
        return kehilanganBarangRepository
                .findByNamaBarangContainingIgnoreCaseAndDihapusFalse(
                        namaBarang
                )
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    // Filter kategori
    @Transactional(readOnly = true)
    public List<KehilanganBarang> filterKehilanganByKategori(
            Integer idKategori
    ) {
        return kehilanganBarangRepository
                .findByKategori_IdKategoriAndDihapusFalse(idKategori)
                .stream()
                .filter(barang -> barang.getStatus() == StatusBarang.TERSEDIA)
                .toList();
    }

    // Mengambil detail laporan yang belum dihapus
    @Transactional(readOnly = true)
    public Optional<KehilanganBarang> getKehilanganById(Long id) {
        return kehilanganBarangRepository
                .findByIdKehilanganAndDihapusFalse(id);
    }

    // Membuat laporan kehilangan
    @Transactional
    public KehilanganBarang createKehilangan(
            KehilanganBarangDTO dto,
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

    // Memeriksa apakah laporan memiliki interaksi yang masih berjalan
    private boolean memilikiInteraksiAktif(Long id) {
        return klaimAtauTanggapanRepository
                .existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                        id,
                        StatusInteraksi.MENUNGGU_VERIFIKASI
                )
                || klaimAtauTanggapanRepository
                .existsByKehilanganBarang_IdKehilanganAndStatusInteraksi(
                        id,
                        StatusInteraksi.DISETUJUI
                );
    }

    // Mengubah laporan
    @Transactional
    public KehilanganBarang updateKehilangan(
            Long id,
            KehilanganBarangDTO dto,
            String email
    ) {
        KehilanganBarang barang = kehilanganBarangRepository
                .findByIdKehilanganAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan kehilangan tidak ditemukan"
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
        barang.setDeskripsiPublik(dto.getDeskripsiPublik());
        barang.setSpesifikasiRahasia(dto.getSpesifikasiRahasia());
        barang.setLokasiKejadian(dto.getLokasiKejadian());
        barang.setTanggalKejadian(dto.getTanggalKejadian());
        barang.setUrlGambar(dto.getUrlGambar());

        return kehilanganBarangRepository.save(barang);
    }

    // Soft delete laporan yang sudah selesai
    @Transactional
    public void deleteKehilangan(Long id, String email) {
        KehilanganBarang barang = kehilanganBarangRepository
                .findByIdKehilanganAndDihapusFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Laporan kehilangan tidak ditemukan"
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
        kehilanganBarangRepository.save(barang);
    }

    // Mengubah entity menjadi response DTO
    @Transactional(readOnly = true)
    public KehilanganBarangResponseDTO toResponseDTO(
            KehilanganBarang barang
    ) {
        KehilanganBarangResponseDTO dto =
                new KehilanganBarangResponseDTO();

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

    // Mengambil laporan arsip yang belum dihapus
    @Transactional(readOnly = true)
    public List<KehilanganBarang> getArsipKehilangan() {
        return kehilanganBarangRepository.findByStatusAndDihapusFalse(
                StatusBarang.DIARSIPKAN
        );
    }
}