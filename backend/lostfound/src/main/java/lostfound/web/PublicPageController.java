package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.model.StatusBarang;
import lostfound.service.AuthService;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.KategoriService;
import lostfound.view.BarangCard;
import lostfound.view.PageView;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumSet;
import java.util.List;

/**
 * Halaman publik: landing page, login, dan registrasi.
 */
@Controller
public class PublicPageController {

    private static final int UKURAN_HALAMAN = 8;

    private final CurrentUserService currentUserService;
    private final BarangQueryService barangQueryService;
    private final KategoriService kategoriService;
    private final AuthService authService;

    public PublicPageController(CurrentUserService currentUserService,
                                BarangQueryService barangQueryService,
                                KategoriService kategoriService,
                                AuthService authService) {
        this.currentUserService = currentUserService;
        this.barangQueryService = barangQueryService;
        this.kategoriService = kategoriService;
        this.authService = authService;
    }

    // Mencegah mass-assignment (peran, provider, id, dst.) pada form registrasi
    @InitBinder("pengguna")
    public void batasiField(WebDataBinder binder) {
        binder.setAllowedFields("namaLengkap", "email", "nomorTelepon", "kataSandi");
    }

    @GetMapping("/")
    public String landing(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String kategori,
            @RequestParam(defaultValue = "") String jenis,
            @RequestParam(defaultValue = "1") String page,
            Authentication authentication,
            Model model) {

        Pengguna login = currentUserService.cari(authentication).orElse(null);
        if (login != null) {
            return "redirect:/dashboard";
        }

        int idKategori = WebSupport.angka(kategori);
        // Feeds hanya memuat laporan TERSEDIA; SELESAI dan DIARSIPKAN disaring di query
        List<BarangCard> semua = barangQueryService.telusuri(
                jenis, q, idKategori, EnumSet.of(StatusBarang.TERSEDIA), null);
        // Landing hanya menampilkan barang terbaru (halaman 1); selebihnya setelah login
        PageView<BarangCard> items = PageView.of(semua, 1, UKURAN_HALAMAN);
        List<BarangCard> terbaru = barangQueryService.terbaru(4, null);

        model.addAttribute("items", items);
        model.addAttribute("terbaru", terbaru);
        model.addAttribute("tahun", java.time.LocalDate.now().getYear());
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("q", q);
        model.addAttribute("kategoriSel", idKategori);
        model.addAttribute("jenisSel", BarangQueryService.normalisasiJenis(jenis) == null
                ? "" : BarangQueryService.normalisasiJenis(jenis).toLowerCase());
        model.addAttribute("filterAktif", !q.isBlank() || idKategori != 0 || !jenis.isBlank());
        return "landing";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String expired,
                        @RequestParam(required = false) String oauth2Error,
                        Model model) {
        // Parameter dibaca di sini (bukan lewat ${param...} di template) karena
        // Thymeleaf 3.1 membatasi akses param pada beberapa konteks.
        model.addAttribute("loginError", error != null);
        model.addAttribute("loggedOut", logout != null);
        model.addAttribute("sessionExpired", expired != null);
        model.addAttribute("oauthError", oauth2Error == null ? "" : oauth2Error);
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("pengguna", new Pengguna());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("pengguna") Pengguna pengguna,
                           @RequestParam(defaultValue = "") String konfirmasi,
                           Model model,
                           RedirectAttributes redirect) {
        try {
            if (pengguna.getKataSandi() == null || !pengguna.getKataSandi().equals(konfirmasi)) {
                throw new IllegalArgumentException("Konfirmasi kata sandi belum sama");
            }
            String email = pengguna.getEmail() == null ? "" : pengguna.getEmail().trim().toLowerCase();
            String telepon = pengguna.getNomorTelepon() == null ? "" : pengguna.getNomorTelepon().trim();
            if (!telepon.isEmpty() && !telepon.matches("^[0-9+\\-\\s]{8,20}$")) {
                throw new IllegalArgumentException(
                        "Nomor telepon hanya boleh berisi angka, spasi, + dan -, panjang 8 sampai 20 karakter");
            }
            if (pengguna.getNamaLengkap() == null || pengguna.getEmail() == null) {
                throw new IllegalArgumentException("Nama lengkap dan email wajib diisi");
            }
            pengguna.setNomorTelepon(telepon.isEmpty() ? null : telepon);
            authService.register(pengguna);
            redirect.addFlashAttribute("emailBaru", email);
            return "redirect:/register/sukses";
        } catch (RuntimeException e) {
            model.addAttribute("error", WebSupport.pesan(e));
            pengguna.setKataSandi(null);
            return "register";
        }
    }

    @GetMapping("/register/sukses")
    public String registerSukses(Model model) {
        Object email = model.asMap().get("emailBaru");
        if (email == null || email.toString().isBlank()) {
            return "redirect:/login";
        }
        return "register-sukses";
    }
}
