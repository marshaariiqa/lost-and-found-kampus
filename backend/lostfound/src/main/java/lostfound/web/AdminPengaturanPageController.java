package lostfound.web;

import lostfound.service.KategoriService;
import lostfound.service.PengaturanService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pengaturan sistem (info posko, aturan klaim) dan master kategori barang.
 */
@Controller
public class AdminPengaturanPageController {

    private final PengaturanService pengaturanService;
    private final KategoriService kategoriService;

    public AdminPengaturanPageController(PengaturanService pengaturanService,
                                         KategoriService kategoriService) {
        this.pengaturanService = pengaturanService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/admin/pengaturan")
    public String tampil(Model model) {
        model.addAttribute("kategoriRows", kategoriService.daftar());
        return "admin/pengaturan";
    }

    @PostMapping("/admin/pengaturan")
    public String simpan(@RequestParam(defaultValue = "") String namaSistem,
                         @RequestParam(defaultValue = "") String email,
                         @RequestParam(defaultValue = "") String lokasi,
                         @RequestParam(defaultValue = "") String jam,
                         @RequestParam(defaultValue = "") String tenggat,
                         Model model, RedirectAttributes redirect) {
        try {
            pengaturanService.simpan(namaSistem, email, lokasi, jam, tenggat);
            redirect.addFlashAttribute("toastSukses", "Pengaturan berhasil disimpan.");
            return "redirect:/admin/pengaturan";
        } catch (RuntimeException e) {
            model.addAttribute("kategoriRows", kategoriService.daftar());
            model.addAttribute("errUmum", WebSupport.pesan(e));
            model.addAttribute("toastError", WebSupport.pesan(e));
            model.addAttribute("inNama", namaSistem);
            model.addAttribute("inEmail", email);
            model.addAttribute("inLokasi", lokasi);
            model.addAttribute("inJam", jam);
            model.addAttribute("inTenggat", tenggat);
            return "admin/pengaturan";
        }
    }

    @PostMapping("/admin/pengaturan/kategori")
    public String tambahKategori(@RequestParam(defaultValue = "") String nama,
                                 RedirectAttributes redirect) {
        try {
            kategoriService.tambah(nama);
            redirect.addFlashAttribute("toastSukses", "Kategori berhasil ditambahkan.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/pengaturan";
    }

    @PostMapping("/admin/pengaturan/kategori/{id}/hapus")
    public String hapusKategori(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            kategoriService.hapus(id);
            redirect.addFlashAttribute("toastSukses", "Kategori berhasil dihapus.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/pengaturan";
    }

    @PostMapping("/admin/pengaturan/kategori/standar")
    public String kategoriStandar(RedirectAttributes redirect) {
        int n = kategoriService.tambahkanStandar();
        redirect.addFlashAttribute(n > 0 ? "toastSukses" : "toastError", n > 0
                ? n + " kategori standar ditambahkan."
                : "Semua kategori standar sudah ada.");
        return "redirect:/admin/pengaturan";
    }
}
