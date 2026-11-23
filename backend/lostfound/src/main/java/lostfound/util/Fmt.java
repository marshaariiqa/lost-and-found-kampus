package lostfound.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Helper format tampilan (tanggal Indonesia, kode laporan, inisial nama).
 */
public final class Fmt {

    private static final String[] BULAN = {
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    };

    private Fmt() {
    }

    public static String tanggal(LocalDateTime t) {
        if (t == null) {
            return "-";
        }
        return t.getDayOfMonth() + " " + BULAN[t.getMonthValue() - 1] + " " + t.getYear();
    }

    public static String tanggalWaktu(LocalDateTime t) {
        if (t == null) {
            return "-";
        }
        return tanggal(t) + ", " + String.format("%02d.%02d", t.getHour(), t.getMinute());
    }

    /** Contoh: "10 Okt, 09.14" (tanpa tahun, dipakai di tabel padat). */
    public static String tanggalSingkat(LocalDateTime t) {
        if (t == null) {
            return "-";
        }
        return t.getDayOfMonth() + " " + BULAN[t.getMonthValue() - 1]
                + ", " + String.format("%02d.%02d", t.getHour(), t.getMinute());
    }

    public static String jam(LocalDateTime t) {
        return t == null ? "-" : String.format("%02d.%02d", t.getHour(), t.getMinute());
    }

    /** Waktu relatif: "Baru saja", "5 menit lalu", "2 jam lalu", atau tanggal. */
    public static String relatif(LocalDateTime t) {
        if (t == null) {
            return "-";
        }
        Duration d = Duration.between(t, LocalDateTime.now());
        long menit = d.toMinutes();
        if (menit < 1) {
            return "Baru saja";
        }
        if (menit < 60) {
            return menit + " menit lalu";
        }
        long jam = d.toHours();
        if (jam < 24 && t.toLocalDate().equals(LocalDate.now())) {
            return jam + " jam lalu";
        }
        return tanggalWaktu(t);
    }

    /** Kelompok notifikasi: "Hari ini", "Kemarin", atau "Sebelumnya". */
    public static String kelompokHari(LocalDateTime t) {
        if (t == null) {
            return "Sebelumnya";
        }
        LocalDate hariIni = LocalDate.now();
        LocalDate tgl = t.toLocalDate();
        if (tgl.equals(hariIni)) {
            return "Hari ini";
        }
        if (tgl.equals(hariIni.minusDays(1))) {
            return "Kemarin";
        }
        return "Sebelumnya";
    }

    /** Hanya mengizinkan foto hasil upload (/uploads/barang/...) atau URL https. */
    public static String urlGambarAman(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String u = url.trim();
        if (u.startsWith("/uploads/barang/") && !u.contains("..")) {
            return u;
        }
        if (u.toLowerCase().startsWith("https://")) {
            return u;
        }
        return null;
    }

    public static String kode(String prefix, Number id) {
        if (id == null) {
            return "-";
        }
        return "#" + prefix + "-" + String.format("%05d", id.longValue());
    }

    public static String inisial(String nama) {
        if (nama == null || nama.isBlank()) {
            return "?";
        }
        String[] kata = nama.trim().split("\\s+");
        String hasil = kata[0].substring(0, 1);
        if (kata.length > 1) {
            hasil += kata[kata.length - 1].substring(0, 1);
        }
        return hasil.toUpperCase();
    }

    public static String namaDepan(String nama) {
        if (nama == null || nama.isBlank()) {
            return "";
        }
        return nama.trim().split("\\s+")[0];
    }

    /** Nama singkat untuk tampilan publik, mis. "Aulia R." */
    public static String namaSingkat(String nama) {
        if (nama == null || nama.isBlank()) {
            return "-";
        }
        String[] kata = nama.trim().split("\\s+");
        if (kata.length == 1) {
            return kata[0];
        }
        return kata[0] + " " + kata[kata.length - 1].substring(0, 1).toUpperCase() + ".";
    }
}
