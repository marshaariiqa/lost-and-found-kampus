package lostfound.web;

import lostfound.model.Pengguna;
import lostfound.service.AdminPenggunaService;
import lostfound.service.AdminPenggunaService.PenggunaRow;
import lostfound.service.CurrentUserService;
import lostfound.service.ExcelExportService;
import lostfound.util.Fmt;
import lostfound.view.PageView;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manajemen pengguna (admin): daftar, ubah peran/status, ekspor Excel.
 */
@Controller
public class AdminPenggunaPageController {

    private static final int UKURAN_HALAMAN = 10;

    private final AdminPenggunaService penggunaService;
    private final CurrentUserService currentUserService;
    private final ExcelExportService excelExportService;

    public AdminPenggunaPageController(AdminPenggunaService penggunaService,
                                       CurrentUserService currentUserService,
                                       ExcelExportService excelExportService) {
        this.penggunaService = penggunaService;
        this.currentUserService = currentUserService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/admin/pengguna")
    public String daftar(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String peran,
                         @RequestParam(defaultValue = "1") String page,
                         @RequestParam(required = false) Integer kelola,
                         Model model) {
        List<PenggunaRow> semua = penggunaService.daftar(q, peran);
        model.addAttribute("items", PageView.of(semua, WebSupport.halaman(page), UKURAN_HALAMAN));
        model.addAttribute("total", penggunaService.hitung());
        model.addAttribute("q", q);
        model.addAttribute("peranSel", peran.toUpperCase());
        if (kelola != null) {
            penggunaService.cari(kelola).ifPresent(u -> model.addAttribute("target", u));
        }
        return "admin/pengguna";
    }

    @PostMapping("/admin/pengguna/{id}")
    public String simpan(@PathVariable Integer id,
                         @RequestParam(defaultValue = "") String peran,
                         @RequestParam(defaultValue = "aktif") String status,
                         Authentication authentication, RedirectAttributes redirect) {
        try {
            Pengguna admin = currentUserService.wajib(authentication);
            penggunaService.perbarui(id, peran, "aktif".equals(status), admin);
            redirect.addFlashAttribute("toastSukses", "Perubahan pengguna disimpan.");
            return "redirect:/admin/pengguna";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
            return "redirect:/admin/pengguna?kelola=" + id;
        }
    }

    @GetMapping("/admin/ekspor/pengguna")
    public ResponseEntity<byte[]> ekspor(@RequestParam(defaultValue = "") String q,
                                         @RequestParam(defaultValue = "") String peran,
                                         Authentication authentication) {
        Pengguna admin = currentUserService.wajib(authentication);
        List<List<Object>> baris = new ArrayList<>();
        for (PenggunaRow r : penggunaService.daftar(q, peran)) {
            List<Object> b = new ArrayList<>();
            b.add(r.nama());
            b.add(r.email());
            b.add(r.telepon());
            b.add(r.admin() ? "Admin" : "Mahasiswa");
            b.add(r.provider());
            b.add(r.aktif() ? "Aktif" : "Nonaktif");
            baris.add(b);
        }

        List<String> filter = new ArrayList<>();
        if (!peran.isBlank()) {
            filter.add("Peran: " + peran);
        }
        if (!q.isBlank()) {
            filter.add("Pencarian: " + q);
        }
        List<String> keterangan = List.of(
                "Diekspor pada " + Fmt.tanggalWaktu(LocalDateTime.now()) + " oleh " + admin.getNamaLengkap(),
                "Filter: " + (filter.isEmpty() ? "Semua data" : String.join("; ", filter)),
                "Jumlah baris: " + baris.size());

        byte[] isi = excelExportService.buat("Pengguna", "FindUp! - Manajemen Pengguna", keterangan,
                List.of("Nama Lengkap", "Email", "Nomor Telepon", "Peran", "Login Via", "Status"), baris);
        String namaFile = "findup-pengguna-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ExcelExportService.CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + namaFile + "\"")
                .header("Cache-Control", "no-store")
                .body(isi);
    }
}
