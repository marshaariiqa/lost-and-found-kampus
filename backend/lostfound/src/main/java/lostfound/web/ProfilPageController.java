package lostfound.web;

import lostfound.service.AuthService;
import lostfound.service.CurrentUserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Profil & Pengaturan akun. Dipakai mahasiswa maupun admin; yang berbeda
 * hanya layout (sidebar) yang dibungkus.
 */
@Controller
public class ProfilPageController {

    private final CurrentUserService currentUserService;
    private final AuthService authService;

    public ProfilPageController(CurrentUserService currentUserService, AuthService authService) {
        this.currentUserService = currentUserService;
        this.authService = authService;
    }

    private void layout(Model model, Authentication authentication) {
        boolean admin = currentUserService.wajib(authentication).getPeran().name().equals("ADMIN");
        model.addAttribute("layoutName", admin ? "layout/adm" : "layout/mhs");
    }

    @GetMapping("/profil")
    public String profil(Authentication authentication, Model model) {
        layout(model, authentication);
        return "akun/profil";
    }

    @GetMapping("/pengaturan")
    public String pengaturan(Authentication authentication, Model model) {
        layout(model, authentication);
        return "akun/pengaturan";
    }

    @PostMapping("/pengaturan/profil")
    public String simpanProfil(@RequestParam(defaultValue = "") String nama,
                               @RequestParam(defaultValue = "") String telepon,
                               Authentication authentication, Model model,
                               RedirectAttributes redirect) {
        try {
            authService.perbaruiProfil(authentication.getName(), nama, telepon);
            redirect.addFlashAttribute("toastSukses", "Profil berhasil diperbarui.");
            return "redirect:/pengaturan";
        } catch (RuntimeException e) {
            layout(model, authentication);
            model.addAttribute("errProfil", WebSupport.pesan(e));
            model.addAttribute("namaInput", nama);
            model.addAttribute("teleponInput", telepon);
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "akun/pengaturan";
        }
    }

    @PostMapping("/pengaturan/sandi")
    public String ubahSandi(@RequestParam(defaultValue = "") String lama,
                            @RequestParam(defaultValue = "") String baru,
                            @RequestParam(defaultValue = "") String konfirmasi,
                            Authentication authentication, Model model,
                            RedirectAttributes redirect) {
        try {
            authService.ubahKataSandi(authentication.getName(), lama, baru, konfirmasi);
            redirect.addFlashAttribute("toastSukses", "Kata sandi berhasil diubah.");
            return "redirect:/pengaturan";
        } catch (RuntimeException e) {
            layout(model, authentication);
            model.addAttribute("errSandi", WebSupport.pesan(e));
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "akun/pengaturan";
        }
    }
}
