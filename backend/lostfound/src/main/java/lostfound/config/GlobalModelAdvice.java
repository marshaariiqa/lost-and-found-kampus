package lostfound.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lostfound.model.Pengguna;
import lostfound.model.StatusInteraksi;
import lostfound.service.AdminBarangService;
import lostfound.service.CurrentUserService;
import lostfound.service.InteraksiViewService;
import lostfound.service.NotifikasiViewService;
import lostfound.service.PengaturanService;
import lostfound.util.Fmt;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Data yang dibutuhkan hampir semua halaman: pengguna login, jumlah
 * notifikasi belum dibaca, info posko, path saat ini, dan toast dari login.
 */
@ControllerAdvice(basePackages = "lostfound.web")
public class GlobalModelAdvice {

    public record UserView(Integer id, String nama, String namaDepan, String inisial,
                           String email, String telepon, String peran, String provider) {
        public boolean admin() {
            return "ADMIN".equals(peran);
        }
    }

    private final CurrentUserService currentUserService;
    private final NotifikasiViewService notifikasiViewService;
    private final PengaturanService pengaturanService;
    private final InteraksiViewService interaksiViewService;
    private final AdminBarangService adminBarangService;

    public GlobalModelAdvice(CurrentUserService currentUserService,
                             NotifikasiViewService notifikasiViewService,
                             PengaturanService pengaturanService,
                             InteraksiViewService interaksiViewService,
                             AdminBarangService adminBarangService) {
        this.interaksiViewService = interaksiViewService;
        this.adminBarangService = adminBarangService;
        this.currentUserService = currentUserService;
        this.notifikasiViewService = notifikasiViewService;
        this.pengaturanService = pengaturanService;
    }

    @ModelAttribute("me")
    public UserView me(Authentication authentication) {
        return currentUserService.cari(authentication).map(this::view).orElse(null);
    }

    @ModelAttribute("unreadCount")
    public long unreadCount(Authentication authentication) {
        return currentUserService.cari(authentication)
                .map(p -> notifikasiViewService.belumDibaca(p.getIdPengguna()))
                .orElse(0L);
    }

    /** Jumlah klaim menunggu verifikasi (lencana sidebar admin). */
    @ModelAttribute("antreanCount")
    public long antreanCount(Authentication authentication) {
        return currentUserService.cari(authentication)
                .filter(p -> p.getPeran().name().equals("ADMIN"))
                .map(p -> interaksiViewService.hitungStatus(StatusInteraksi.MENUNGGU_VERIFIKASI))
                .orElse(0L);
    }

    /** Jumlah laporan temuan yang menunggu diterima di Posko (lencana menu Inventaris admin). */
    @ModelAttribute("penerimaanCount")
    public long penerimaanCount(Authentication authentication) {
        return currentUserService.cari(authentication)
                .filter(p -> p.getPeran().name().equals("ADMIN"))
                .map(p -> adminBarangService.hitungMenungguPenerimaan())
                .orElse(0L);
    }

    @ModelAttribute("posko")
    public PengaturanService.Posko posko() {
        return pengaturanService.posko();
    }

    @ModelAttribute("reqPath")
    public String reqPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /** Toast sukses dari proses login; dibaca & dihapus satu kali. */
    @ModelAttribute("toastLogin")
    public String toastLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object nilai = session.getAttribute(LoginSuccessHandler.ATTR_TOAST);
        if (nilai == null) {
            return null;
        }
        session.removeAttribute(LoginSuccessHandler.ATTR_TOAST);
        return nilai.toString();
    }

    private UserView view(Pengguna p) {
        return new UserView(p.getIdPengguna(), p.getNamaLengkap(),
                Fmt.namaDepan(p.getNamaLengkap()), Fmt.inisial(p.getNamaLengkap()),
                p.getEmail(),
                p.getNomorTelepon() == null || p.getNomorTelepon().isBlank() ? null : p.getNomorTelepon(),
                p.getPeran().name(), p.getProvider().name());
    }
}
