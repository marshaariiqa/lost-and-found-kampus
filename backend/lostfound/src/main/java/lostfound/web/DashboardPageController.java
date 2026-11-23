package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.DashboardService;
import lostfound.service.KategoriService;
import lostfound.view.BarangCard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Beranda mahasiswa. Admin diarahkan ke /admin.
 */
@Controller
public class DashboardPageController {

    private final CurrentUserService currentUserService;
    private final DashboardService dashboardService;
    private final BarangQueryService barangQueryService;
    private final KategoriService kategoriService;

    public DashboardPageController(CurrentUserService currentUserService,
                                   DashboardService dashboardService,
                                   BarangQueryService barangQueryService,
                                   KategoriService kategoriService) {
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
        this.barangQueryService = barangQueryService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Pengguna saya = currentUserService.wajib(authentication);
        if (saya.getPeran().name().equals("ADMIN")) {
            return "redirect:/admin";
        }

        // Beranda hanya pratinjau (4 barang terbaru). Pencarian & filter ada di /barang.
        model.addAttribute("stat", dashboardService.mahasiswa(saya.getIdPengguna()));
        model.addAttribute("terbaru", barangQueryService.terbaru(4, saya.getIdPengguna()));
        return "mhs/dashboard";
    }
}
