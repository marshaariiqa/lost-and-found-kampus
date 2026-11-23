package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.service.CurrentUserService;
import lostfound.service.NotifikasiService;
import lostfound.service.NotifikasiViewService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotifikasiPageController {

    private final CurrentUserService currentUserService;
    private final NotifikasiViewService viewService;
    private final NotifikasiService notifikasiService;

    public NotifikasiPageController(CurrentUserService currentUserService,
                                    NotifikasiViewService viewService,
                                    NotifikasiService notifikasiService) {
        this.currentUserService = currentUserService;
        this.viewService = viewService;
        this.notifikasiService = notifikasiService;
    }

    @GetMapping("/notifikasi")
    public String daftar(Authentication authentication, Model model) {
        Pengguna saya = currentUserService.wajib(authentication);
        List<NotifikasiViewService.NotifView> semua = viewService.daftar(saya.getIdPengguna());
        Map<String, List<NotifikasiViewService.NotifView>> grup = new LinkedHashMap<>();
        for (String nama : List.of("Hari ini", "Kemarin", "Sebelumnya")) {
            List<NotifikasiViewService.NotifView> isi = new ArrayList<>();
            for (NotifikasiViewService.NotifView n : semua) {
                if (n.kelompok().equals(nama)) {
                    isi.add(n);
                }
            }
            if (!isi.isEmpty()) {
                grup.put(nama, isi);
            }
        }
        model.addAttribute("grup", grup);
        model.addAttribute("totalNotif", semua.size());
        return "mhs/notifikasi";
    }

    @PostMapping("/notifikasi/{id}/buka")
    public String buka(@PathVariable Long id, Authentication authentication,
                       RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        try {
            return "redirect:" + viewService.buka(id, saya);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
            return "redirect:/notifikasi";
        }
    }

    @PostMapping("/notifikasi/baca-semua")
    public String bacaSemua(Authentication authentication, RedirectAttributes redirect) {
        int jumlah = notifikasiService.tandaiSemuaDibaca(authentication);
        redirect.addFlashAttribute("toastSukses", jumlah > 0
                ? "Semua notifikasi ditandai sudah dibaca."
                : "Tidak ada notifikasi baru.");
        return "redirect:/notifikasi";
    }
}
