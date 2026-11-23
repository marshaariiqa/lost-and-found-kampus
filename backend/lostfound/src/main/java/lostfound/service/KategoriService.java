package lostfound.service;

import lostfound.model.Kategori;
import lostfound.repository.KategoriRepository;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.PenemuBarangRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Master data kategori barang. Tabel dibiarkan kosong saat instalasi;
 * admin menambahkan sendiri, atau menekan tombol "Tambahkan kategori standar".
 */
@Service
public class KategoriService {

    public static final List<String> KATEGORI_STANDAR = List.of(
            "Dompet & Aksesoris", "Elektronik", "Kunci", "Dokumen & KTM",
            "Tas", "Pakaian", "Lainnya");

    public record KategoriRow(Integer id, String nama, boolean dipakai) {
    }

    private final KategoriRepository kategoriRepository;
    private final KehilanganBarangRepository kehilanganRepository;
    private final PenemuBarangRepository penemuRepository;

    public KategoriService(KategoriRepository kategoriRepository,
                           KehilanganBarangRepository kehilanganRepository,
                           PenemuBarangRepository penemuRepository) {
        this.kategoriRepository = kategoriRepository;
        this.kehilanganRepository = kehilanganRepository;
        this.penemuRepository = penemuRepository;
    }

    @Transactional(readOnly = true)
    public List<Kategori> semua() {
        return kategoriRepository.findAll(Sort.by("idKategori"));
    }

    @Transactional(readOnly = true)
    public List<KategoriRow> daftar() {
        List<KategoriRow> hasil = new ArrayList<>();
        for (Kategori k : semua()) {
            boolean dipakai = kehilanganRepository.existsByKategori_IdKategori(k.getIdKategori())
                    || penemuRepository.existsByKategori_IdKategori(k.getIdKategori());
            hasil.add(new KategoriRow(k.getIdKategori(), k.getNamaKategori(), dipakai));
        }
        return hasil;
    }

    @Transactional
    public void tambah(String nama) {
        String bersih = nama == null ? "" : nama.trim();
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Nama kategori wajib diisi");
        }
        if (bersih.length() > 100) {
            throw new IllegalArgumentException("Nama kategori maksimal 100 karakter");
        }
        boolean sudahAda = semua().stream()
                .anyMatch(k -> k.getNamaKategori().equalsIgnoreCase(bersih));
        if (sudahAda) {
            throw new IllegalArgumentException("Kategori \"" + bersih + "\" sudah ada");
        }
        Kategori kategori = new Kategori();
        kategori.setNamaKategori(bersih);
        kategoriRepository.save(kategori);
    }

    @Transactional
    public void hapus(Integer id) {
        Kategori kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
        if (kehilanganRepository.existsByKategori_IdKategori(id)
                || penemuRepository.existsByKategori_IdKategori(id)) {
            throw new IllegalArgumentException(
                "Kategori masih dipakai laporan dan tidak dapat dihapus");
        }
        kategoriRepository.delete(kategori);
    }

    /** Menambahkan kategori standar yang belum ada. Mengembalikan jumlah yang ditambahkan. */
    @Transactional
    public int tambahkanStandar() {
        int ditambah = 0;
        List<Kategori> ada = semua();
        for (String nama : KATEGORI_STANDAR) {
            boolean sudah = ada.stream().anyMatch(k -> k.getNamaKategori().equalsIgnoreCase(nama));
            if (!sudah) {
                Kategori k = new Kategori();
                k.setNamaKategori(nama);
                kategoriRepository.save(k);
                ditambah++;
            }
        }
        return ditambah;
    }
}
