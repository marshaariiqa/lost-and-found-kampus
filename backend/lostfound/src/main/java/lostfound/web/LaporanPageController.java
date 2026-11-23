package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.KehilanganBarangService;
import lostfound.service.PenemuBarangService;
import lostfound.view.BarangCard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Laporan Saya: hanya laporan milik pengguna yang sedang login.
 */
@Controller
public class LaporanPageController {

    private final CurrentUserService currentUserService;
    private final BarangQueryService barangQueryService;
    private final KehilanganBarangService kehilanganService;
    private final PenemuBarangService penemuService;

    public LaporanPageController(CurrentUserService currentUserService,
                                 BarangQueryService barangQueryService,
                                 KehilanganBarangService kehilanganService,
                                 PenemuBarangService penemuService) {
        this.currentUserService = currentUserService;
        this.barangQueryService = barangQueryService;
        this.kehilanganService = kehilanganService;
        this.penemuService = penemuService;
    }

    @GetMapping("/laporan")
    public String daftar(@RequestParam(defaultValue = "") String jenis,
                         @RequestParam(defaultValue = "") String status,
                         Authentication authentication,
                         Model model) {
        Pengguna saya = currentUserService.wajib(authentication);
        List<BarangCard> semua = barangQueryService.milikSaya(saya.getIdPengguna());

        String j = BarangQueryService.normalisasiJenis(jenis);
        String st = status.trim().toLowerCase();
        // Hitungan chip status mengikuti filter jenis yang sedang dipilih
        List<BarangCard> sesuaiJenis = semua.stream()
                .filter(c -> j == null || c.jenis().equals(j))
                .toList();
        List<BarangCard> tampil = sesuaiJenis.stream()
                .filter(c -> st.isEmpty()
                        // "active" = semua laporan yang masih berjalan (dipakai kartu Laporan Aktif di Beranda)
                        || ("active".equals(st) ? !(c.alur().equals("done") || c.alur().equals("arch"))
                                                : c.alur().equals(st)))
                .toList();
        for (String kode : List.of("avail", "wait", "ok", "done", "no", "arch")) {
            model.addAttribute("n_" + kode,
                    sesuaiJenis.stream().filter(c -> c.alur().equals(kode)).count());
        }
        model.addAttribute("nSemua", sesuaiJenis.size());

        model.addAttribute("rows", tampil);
        model.addAttribute("total", semua.size());
        model.addAttribute("totalHilang", semua.stream().filter(BarangCard::hilang).count());
        model.addAttribute("totalTemuan", semua.stream().filter(c -> !c.hilang()).count());
        model.addAttribute("jenisSel", j == null ? "" : j.toLowerCase());
        model.addAttribute("statusSel", st);
        return "mhs/laporan";
    }

    @PostMapping("/laporan/{jenis}/{id}/hapus")
    public String hapus(@PathVariable String jenis,
                        @PathVariable Long id,
                        Authentication authentication,
                        RedirectAttributes redirect) {
        try {
            String j = BarangQueryService.normalisasiJenis(jenis);
            if (BarangQueryService.HILANG.equals(j)) {
                kehilanganService.deleteKehilangan(id, authentication.getName());
            } else if (BarangQueryService.TEMUAN.equals(j)) {
                penemuService.deleteTemuan(id, authentication.getName());
            } else {
                throw new IllegalArgumentException("Jenis laporan tidak valid");
            }
            redirect.addFlashAttribute("toastSukses", "Laporan berhasil dihapus.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/laporan";
    }
}
