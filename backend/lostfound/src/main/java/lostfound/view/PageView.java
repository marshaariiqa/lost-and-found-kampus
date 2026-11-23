package lostfound.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hasil paginasi sederhana (di memori) untuk daftar di halaman web.
 */
public record PageView<T>(List<T> items, int page, int totalPages, long total, int from, int to) {

    public static <T> PageView<T> of(List<T> semua, int halaman, int ukuran) {
        int total = semua.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) ukuran));
        int page = Math.min(Math.max(1, halaman), totalPages);
        int dari = (page - 1) * ukuran;
        int sampai = Math.min(total, dari + ukuran);
        List<T> potongan = total == 0 ? Collections.<T>emptyList()
                : new ArrayList<>(semua.subList(dari, sampai));
        return new PageView<>(potongan, page, totalPages, total, total == 0 ? 0 : dari + 1, sampai);
    }

    public List<Integer> halaman() {
        List<Integer> nomor = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            nomor.add(i);
        }
        return nomor;
    }

    public boolean adaSebelumnya() {
        return page > 1;
    }

    public boolean adaBerikutnya() {
        return page < totalPages;
    }
}
