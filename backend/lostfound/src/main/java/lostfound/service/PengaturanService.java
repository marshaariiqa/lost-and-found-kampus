package lostfound.service;

import lostfound.model.Pengaturan;
import lostfound.repository.PengaturanRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Pengaturan sistem (info posko & aturan klaim). Nilai bawaan ada di kode;
 * baris di tabel pengaturan_sistem hanya dibuat saat admin menyimpan.
 */
@Service
public class PengaturanService {

    public static final String NAMA_SISTEM = "sistem.nama";
    public static final String EMAIL_POSKO = "posko.email";
    public static final String LOKASI_POSKO = "posko.lokasi";
    public static final String JAM_POSKO = "posko.jam";
    public static final String TENGGAT_HARI = "klaim.tenggat_hari";

    public record Posko(String namaSistem, String email, String lokasi, String jam, int tenggatHari) {
        public boolean adaEmail() {
            return email != null && !email.isBlank();
        }
    }

    private final PengaturanRepository repository;

    public PengaturanService(PengaturanRepository repository) {
        this.repository = repository;
    }

    private static String bawaan(String kunci) {
        return switch (kunci) {
            case NAMA_SISTEM -> "FindUp! Lost & Found FIK UPNVJ";
            case EMAIL_POSKO -> "";
            case LOKASI_POSKO -> "Posko FIK";
            case JAM_POSKO -> "08.00 - 16.00 WIB";
            case TENGGAT_HARI -> "2";
            default -> "";
        };
    }

    @Transactional(readOnly = true)
    public String ambil(String kunci) {
        return repository.findById(kunci).map(Pengaturan::getNilai).orElse(bawaan(kunci));
    }

    @Transactional(readOnly = true)
    public Posko posko() {
        return new Posko(
                ambil(NAMA_SISTEM),
                ambil(EMAIL_POSKO),
                ambil(LOKASI_POSKO),
                ambil(JAM_POSKO),
                tenggatHari());
    }

    @Transactional(readOnly = true)
    public int tenggatHari() {
        try {
            int hari = Integer.parseInt(ambil(TENGGAT_HARI).trim());
            return Math.min(Math.max(hari, 1), 30);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    @Transactional
    public void simpan(String namaSistem, String email, String lokasi, String jam, String tenggat) {
        String nama = trim(namaSistem);
        String lokasiBersih = trim(lokasi);
        String jamBersih = trim(jam);
        String emailBersih = trim(email);

        if (nama.isEmpty()) {
            throw new IllegalArgumentException("Nama sistem wajib diisi");
        }
        if (lokasiBersih.isEmpty()) {
            throw new IllegalArgumentException("Lokasi posko wajib diisi");
        }
        if (jamBersih.isEmpty()) {
            throw new IllegalArgumentException("Jam operasional wajib diisi");
        }
        if (!emailBersih.isEmpty() && !emailBersih.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Format email kontak tidak valid");
        }

        int hari;
        try {
            hari = Integer.parseInt(trim(tenggat));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tenggat tanggapan harus berupa angka");
        }
        if (hari < 1 || hari > 30) {
            throw new IllegalArgumentException("Tenggat tanggapan harus antara 1 dan 30 hari");
        }

        repository.saveAll(List.of(
                new Pengaturan(NAMA_SISTEM, potong(nama)),
                new Pengaturan(EMAIL_POSKO, potong(emailBersih)),
                new Pengaturan(LOKASI_POSKO, potong(lokasiBersih)),
                new Pengaturan(JAM_POSKO, potong(jamBersih)),
                new Pengaturan(TENGGAT_HARI, String.valueOf(hari))));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String potong(String s) {
        return s.length() > 480 ? s.substring(0, 480) : s;
    }
}
