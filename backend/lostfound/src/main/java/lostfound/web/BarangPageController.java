package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.model.StatusBarang;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.KategoriService;
import lostfound.view.BarangCard;
import lostfound.view.BarangDetailView;
import lostfound.view.PageView;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Jelajahi barang (hilang & temuan) dan halaman detail.
 */
@Controller
public class BarangPageController {

    private static final int UKURAN_HALAMAN = 8;

    private final CurrentUserService currentUserService;
    private final BarangQueryService barangQueryService;
    private final KategoriService kategoriService;

    public BarangPageController(CurrentUserService currentUserService,
                                BarangQueryService barangQueryService,
                                KategoriService kategoriService) {
        this.currentUserService = currentUserService;
        this.barangQueryService = barangQueryService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/barang")
    public String jelajah(@RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "") String kategori,
                          @RequestParam(defaultValue = "") String jenis,
                          @RequestParam(defaultValue = "terbaru") String urut,
                          @RequestParam(defaultValue = "1") String page,
                          Authentication authentication,
                          Model model) {
        Pengguna saya = currentUserService.wajib(authentication);
        int idKategori = WebSupport.angka(kategori);

        List<BarangCard> semua = new ArrayList<>(barangQueryService.telusuri(
                jenis, q, idKategori, EnumSet.of(StatusBarang.TERSEDIA), saya.getIdPengguna()));
        String urutan = switch (urut) {
            case "terlama", "nama" -> urut;
            default -> "terbaru";
        };
        if ("terlama".equals(urutan)) {
            semua.sort(Comparator.comparing(BarangCard::dibuat, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("nama".equals(urutan)) {
            semua.sort(Comparator.comparing(BarangCard::nama, String.CASE_INSENSITIVE_ORDER));
        }
        PageView<BarangCard> items = PageView.of(semua, WebSupport.halaman(page), UKURAN_HALAMAN);

        String jenisNorm = BarangQueryService.normalisasiJenis(jenis);
        model.addAttribute("items", items);
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("q", q);
        model.addAttribute("kategoriSel", idKategori);
        model.addAttribute("jenisSel", jenisNorm == null ? "" : jenisNorm.toLowerCase());
        model.addAttribute("urutSel", urutan);
        model.addAttribute("filterAktif", !q.isBlank() || idKategori != 0
                || !jenis.isBlank() || !"terbaru".equals(urutan));
        return "mhs/jelajah";
    }

    @GetMapping("/barang/{jenis}/{id}")
    public String detail(@PathVariable String jenis,
                         @PathVariable Long id,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        Optional<BarangDetailView> detail = barangQueryService.detail(jenis, id, saya);
        if (detail.isEmpty()) {
            redirect.addFlashAttribute("toastError", "Laporan tidak ditemukan atau sudah dihapus.");
            return "redirect:/barang";
        }
        model.addAttribute("d", detail.get());
        return "mhs/detail";
    }
}
