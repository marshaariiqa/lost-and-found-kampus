package lostfound.web;

import lostfound.dto.PenemuBarangDTO;
import lostfound.model.PenemuBarang;
import lostfound.model.Pengguna;
import lostfound.service.AdminBarangService;
import lostfound.service.AdminBarangService.BarangRow;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.DashboardService;
import lostfound.service.ExcelExportService;
import lostfound.service.FileStorageService;
import lostfound.service.InteraksiViewService;
import lostfound.service.InteraksiViewService.KlaimRow;
import lostfound.service.KategoriService;
import lostfound.service.KehilanganBarangService;
import lostfound.service.KlaimAtauTanggapanService;
import lostfound.service.LogAktivitasService;
import lostfound.service.PenemuBarangService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Halaman admin operasional: dashboard, antrean verifikasi, inventaris,
 * riwayat penyerahan, arsip, dan ekspor Excel. Hanya ROLE_ADMIN
 * (lihat SecurityConfig: /admin/** hasRole ADMIN).
 */
@Controller
public class AdminPageController {

    private static final int UKURAN_HALAMAN = 10;

    private final CurrentUserService currentUserService;
    private final DashboardService dashboardService;
    private final InteraksiViewService interaksiViewService;
    private final KlaimAtauTanggapanService klaimService;
    private final AdminBarangService adminBarangService;
    private final KategoriService kategoriService;
    private final PenemuBarangService penemuService;
    private final KehilanganBarangService kehilanganService;
    private final LogAktivitasService logAktivitasService;
    private final FileStorageService fileStorageService;
    private final ExcelExportService excelExportService;

    public AdminPageController(CurrentUserService currentUserService,
                               DashboardService dashboardService,
                               InteraksiViewService interaksiViewService,
                               KlaimAtauTanggapanService klaimService,
                               AdminBarangService adminBarangService,
                               KategoriService kategoriService,
                               PenemuBarangService penemuService,
                               KehilanganBarangService kehilanganService,
                               LogAktivitasService logAktivitasService,
                               FileStorageService fileStorageService,
                               ExcelExportService excelExportService) {
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
        this.interaksiViewService = interaksiViewService;
        this.klaimService = klaimService;
        this.adminBarangService = adminBarangService;
        this.kategoriService = kategoriService;
        this.penemuService = penemuService;
        this.kehilanganService = kehilanganService;
        this.logAktivitasService = logAktivitasService;
        this.fileStorageService = fileStorageService;
        this.excelExportService = excelExportService;
    }

    // ================= Dashboard =================

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("stat", dashboardService.admin());
        return "admin/dashboard";
    }

    // ================= Antrean verifikasi =================

    @GetMapping("/admin/antrean")
    public String antrean(@RequestParam(defaultValue = "wait") String status,
                          @RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "1") String page,
                          @RequestParam(required = false) Long verif,
                          Model model) {
        String st = status.isBlank() ? "all" : status.toLowerCase();
        List<KlaimRow> daftar = interaksiViewService.antrean(st, q);
        model.addAttribute("items", PageView.of(daftar, WebSupport.halaman(page), UKURAN_HALAMAN));
        model.addAttribute("statusSel", st);
        model.addAttribute("q", q);
        model.addAttribute("nMenunggu", interaksiViewService.hitungStatus(
                lostfound.model.StatusInteraksi.MENUNGGU_VERIFIKASI));
        model.addAttribute("nDisetujui", interaksiViewService.hitungStatus(
                lostfound.model.StatusInteraksi.DISETUJUI));
        model.addAttribute("nSemua", interaksiViewService.hitungSemua());

        if (verif != null) {
            interaksiViewService.detailAdmin(verif).ifPresent(d -> model.addAttribute("verifDetail", d));
        }
        return "admin/antrean";
    }

    @PostMapping("/admin/antrean/{id}/setujui")
    public String setujui(@PathVariable Long id, Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            var detail = interaksiViewService.detailAdmin(id);
            if (detail.isPresent() && detail.get().perluPenerimaan()) {
                redirect.addFlashAttribute("toastError",
                        "Barang belum diterima di Posko. Terima dulu di Inventaris > Menunggu Penerimaan.");
                return "redirect:/admin/antrean?verif=" + id;
            }
            klaimService.setujuiInteraksi(id, authentication);
            redirect.addFlashAttribute("toastSukses", "Klaim disetujui.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/antrean";
    }

    @PostMapping("/admin/antrean/{id}/tolak")
    public String tolak(@PathVariable Long id,
                        @RequestParam(defaultValue = "") String alasan,
                        Authentication authentication, RedirectAttributes redirect) {
        if (alasan.isBlank()) {
            redirect.addFlashAttribute("toastError", "Alasan penolakan wajib diisi.");
            return "redirect:/admin/antrean?verif=" + id;
        }
        try {
            klaimService.tolakInteraksi(id, alasan.trim(), authentication);
            redirect.addFlashAttribute("toastSukses", "Klaim ditolak.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/antrean";
    }

    @PostMapping("/admin/antrean/{id}/serahkan")
    public String serahkan(@PathVariable Long id, Authentication authentication,
                           RedirectAttributes redirect) {
        try {
            klaimService.selesaikanInteraksi(id, authentication);
            redirect.addFlashAttribute("toastSukses", "Barang berhasil diserahkan.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/antrean?status=ok";
    }

    // ================= Inventaris Posko =================

    /**
     * Inventaris = barang temuan yang SUDAH diterima fisik di Posko.
     * Tab "penerimaan" berisi laporan temuan yang belum diserahkan ke Posko.
     */
    @GetMapping("/admin/inventaris")
    public String inventaris(@RequestParam(defaultValue = "posko") String tab,
                             @RequestParam(defaultValue = "") String q,
                             @RequestParam(defaultValue = "") String kategori,
                             @RequestParam(defaultValue = "") String status,
                             @RequestParam(defaultValue = "1") String page,
                             Model model) {
        int idKategori = WebSupport.angka(kategori);
        boolean penerimaan = "penerimaan".equals(tab);
        List<BarangRow> daftar = penerimaan
                ? adminBarangService.menungguPenerimaan(q, idKategori)
                : adminBarangService.inventarisPosko(q, idKategori, status);

        model.addAttribute("items", PageView.of(daftar, WebSupport.halaman(page), UKURAN_HALAMAN));
        model.addAttribute("tab", penerimaan ? "penerimaan" : "posko");
        model.addAttribute("nPosko", adminBarangService.hitungInventaris());
        model.addAttribute("nPenerimaan", adminBarangService.hitungMenungguPenerimaan());
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("q", q);
        model.addAttribute("kategoriSel", idKategori);
        model.addAttribute("statusSel", status.toLowerCase());
        return "admin/inventaris";
    }

    @PostMapping("/admin/inventaris/{id}/terima")
    public String terimaDiPosko(@PathVariable Long id,
                                Authentication authentication, RedirectAttributes redirect) {
        try {
            Pengguna admin = currentUserService.wajib(authentication);
            // Lokasi penyimpanan otomatis = lokasi Posko dari Pengaturan Sistem
            adminBarangService.terimaDiPosko(id, null, admin);
            redirect.addFlashAttribute("toastSukses", "Barang berhasil diterima di Posko.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/inventaris?tab=penerimaan";
    }

    @GetMapping("/admin/inventaris/baru")
    public String barangBaru(Model model) {
        isiFormBarang(model, new FormBarang("", "", "", "", "", ""), new LinkedHashMap<>());
        return "admin/barang-form";
    }

    public record FormBarang(String nama, String idKategori, String lokasi,
                             String tanggal, String simpan, String deskripsi) {
    }

    @PostMapping("/admin/inventaris/baru")
    public String simpanBarang(@RequestParam(defaultValue = "") String nama,
                               @RequestParam(defaultValue = "") String idKategori,
                               @RequestParam(defaultValue = "") String lokasi,
                               @RequestParam(defaultValue = "") String tanggal,
                               @RequestParam(defaultValue = "") String simpan,
                               @RequestParam(defaultValue = "") String deskripsi,
                               @RequestParam(name = "foto", required = false) MultipartFile foto,
                               Authentication authentication, Model model,
                               RedirectAttributes redirect) {
        FormBarang form = new FormBarang(nama.trim(), idKategori, lokasi.trim(),
                tanggal.trim(), simpan.trim(), deskripsi.trim());
        Map<String, String> errors = new LinkedHashMap<>();

        if (form.nama().isEmpty()) {
            errors.put("nama", "Nama barang wajib diisi");
        }
        int kategoriId = WebSupport.angka(idKategori);
        if (kategoriId == 0) {
            errors.put("idKategori", "Kategori wajib dipilih");
        }
        if (form.lokasi().isEmpty()) {
            errors.put("lokasi", "Lokasi ditemukan wajib diisi");
        }
        if (form.deskripsi().isEmpty()) {
            errors.put("deskripsi", "Deskripsi wajib diisi");
        }
        LocalDate tgl = null;
        if (form.tanggal().isEmpty()) {
            errors.put("tanggal", "Tanggal wajib diisi");
        } else {
            try {
                tgl = LocalDate.parse(form.tanggal());
                if (tgl.isAfter(LocalDate.now())) {
                    errors.put("tanggal", "Tanggal tidak boleh di masa depan");
                }
            } catch (DateTimeParseException e) {
                errors.put("tanggal", "Format tanggal tidak valid");
            }
        }

        String urlFoto = null;
        if (errors.isEmpty() && foto != null && !foto.isEmpty()) {
            try {
                urlFoto = fileStorageService.simpanFotoBarang(foto);
            } catch (IllegalArgumentException e) {
                errors.put("foto", e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            isiFormBarang(model, form, errors);
            model.addAttribute("toastError", "Periksa kembali kolom yang ditandai.");
            return "admin/barang-form";
        }

        try {
            Pengguna admin = currentUserService.wajib(authentication);
            PenemuBarangDTO dto = new PenemuBarangDTO();
            dto.setIdKategori(kategoriId);
            dto.setNamaBarang(form.nama());
            dto.setDeskripsi(form.deskripsi());
            dto.setLokasiDitemukan(form.lokasi());
            dto.setTanggalDitemukan(tgl.atStartOfDay());
            dto.setUrlGambar(urlFoto);
            dto.setLokasiPenyimpanan(form.simpan().isEmpty() ? null : form.simpan());

            PenemuBarang barang = penemuService.createTemuan(dto, authentication.getName());
            // Barang yang dicatat admin berarti sudah ada fisiknya di Posko
            adminBarangService.terimaDiPosko(barang.getIdPenemu(), null, admin);

            redirect.addFlashAttribute("toastSukses", "Barang berhasil ditambahkan ke inventaris.");
            return "redirect:/admin/inventaris";
        } catch (RuntimeException e) {
            fileStorageService.hapus(urlFoto);
            isiFormBarang(model, form, errors);
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "admin/barang-form";
        }
    }

    private void isiFormBarang(Model model, FormBarang form, Map<String, String> errors) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("hariIni", LocalDate.now().toString());
    }

    // ================= Riwayat penyerahan =================

    @GetMapping("/admin/riwayat")
    public String riwayat(@RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "") String jenis,
                          @RequestParam(defaultValue = "1") String page,
                          Model model) {
        List<KlaimRow> daftar = interaksiViewService.riwayat(q, jenis);
        model.addAttribute("items", PageView.of(daftar, WebSupport.halaman(page), UKURAN_HALAMAN));
        model.addAttribute("q", q);
        model.addAttribute("jenisSel", normJenis(jenis));
        return "admin/riwayat";
    }

    // ================= Arsip =================

    @GetMapping("/admin/arsip")
    public String arsip(@RequestParam(defaultValue = "") String q,
                        @RequestParam(defaultValue = "") String kategori,
                        @RequestParam(defaultValue = "") String jenis,
                        @RequestParam(defaultValue = "1") String page,
                        Model model) {
        int idKategori = WebSupport.angka(kategori);
        List<BarangRow> daftar = adminBarangService.arsip(q, idKategori, jenis);
        model.addAttribute("items", PageView.of(daftar, WebSupport.halaman(page), UKURAN_HALAMAN));
        model.addAttribute("total", adminBarangService.hitungArsip());
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("q", q);
        model.addAttribute("kategoriSel", idKategori);
        model.addAttribute("jenisSel", normJenis(jenis));
        return "admin/arsip";
    }

    @PostMapping("/admin/arsip/{jenis}/{id}/pulihkan")
    public String pulihkan(@PathVariable String jenis, @PathVariable Long id,
                           RedirectAttributes redirect) {
        try {
            String j = BarangQueryService.normalisasiJenis(jenis);
            if (BarangQueryService.HILANG.equals(j)) {
                kehilanganService.restoreKehilangan(id);
            } else if (BarangQueryService.TEMUAN.equals(j)) {
                penemuService.restoreTemuan(id);
            } else {
                throw new IllegalArgumentException("Jenis laporan tidak valid");
            }
            redirect.addFlashAttribute("toastSukses", "Barang dipulihkan ke inventaris.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("toastError", WebSupport.pesan(e));
        }
        return "redirect:/admin/arsip";
    }

    // ================= Ekspor Excel =================

    /**
     * Ekspor mengikuti filter layar yang sama (parameter query identik dengan halaman
     * daftar). Layar: antrean, inventaris, riwayat, arsip.
     */
    @GetMapping("/admin/ekspor/{layar}")
    public ResponseEntity<byte[]> ekspor(@PathVariable String layar,
                                         @RequestParam(defaultValue = "") String q,
                                         @RequestParam(defaultValue = "") String kategori,
                                         @RequestParam(defaultValue = "") String jenis,
                                         @RequestParam(defaultValue = "") String status,
                                         Authentication authentication) {
        Pengguna admin = currentUserService.wajib(authentication);
        int idKategori = WebSupport.angka(kategori);
        List<String> header;
        List<List<Object>> baris = new ArrayList<>();
        String judul;
        String sheet;
        List<String> filter = new ArrayList<>();

        switch (layar) {
            case "antrean" -> {
                String st = status.isBlank() ? "all" : status.toLowerCase();
                judul = "FindUp! - Antrean Verifikasi";
                sheet = "Antrean Verifikasi";
                header = List.of("Kode Klaim", "Barang", "Kode Barang", "Jenis Laporan", "Pemohon",
                        "Email Pemohon", "Telepon Pemohon", "Diajukan", "Status", "Diverifikasi Oleh",
                        "Waktu Verifikasi", "Alasan Penolakan");
                for (KlaimRow r : interaksiViewService.antrean(st, q)) {
                    baris.add(list(r.kode(), r.namaBarang(), r.kodeBarang(), r.jenisInteraksi(),
                            r.pemohon(), r.emailPemohon(),
                            r.teleponPemohon() == null ? "-" : r.teleponPemohon(),
                            r.diajukan(), r.statusLabel(), r.diverifikasiOleh(),
                            r.waktuVerifikasi(), r.alasanPenolakan() == null ? "-" : r.alasanPenolakan()));
                }
                filter.add("Status: " + labelStatusAntrean(st));
                if (!q.isBlank()) {
                    filter.add("Pencarian: " + q);
                }
            }
            case "inventaris" -> {
                judul = "FindUp! - Inventaris Posko";
                sheet = "Inventaris Posko";
                header = List.of("Kode", "Nama Barang", "Kategori", "Lokasi Ditemukan",
                        "Lokasi Penyimpanan", "Tanggal Ditemukan", "Diterima di Posko", "Pelapor", "Status");
                for (BarangRow r : adminBarangService.inventarisPosko(q, idKategori, status)) {
                    baris.add(list(r.kode(), r.nama(), r.kategori(), r.lokasi(), r.lokasiPenyimpanan(),
                            r.tanggal(), r.diterima(), r.pelapor(), r.statusLabel()));
                }
                tambahFilterBarang(filter, q, idKategori, "", status);
            }
            case "riwayat" -> {
                judul = "FindUp! - Riwayat Penyerahan";
                sheet = "Riwayat Penyerahan";
                header = List.of("Kode Klaim", "Kode Barang", "Barang", "Jenis", "Penerima",
                        "Email Penerima", "Tanggal Serah", "Petugas", "Status");
                for (KlaimRow r : interaksiViewService.riwayat(q, jenis)) {
                    baris.add(list(r.kode(), r.kodeBarang(), r.namaBarang(), r.jenisInteraksi(),
                            r.pemohon(), r.emailPemohon(), r.waktuSelesai(),
                            r.diverifikasiOleh(), "Selesai"));
                }
                if (!jenis.isBlank()) {
                    filter.add("Jenis: " + jenis);
                }
                if (!q.isBlank()) {
                    filter.add("Pencarian: " + q);
                }
            }
            case "arsip" -> {
                judul = "FindUp! - Arsip Barang";
                sheet = "Arsip Barang";
                header = List.of("Kode", "Nama Barang", "Jenis", "Kategori", "Lokasi",
                        "Dilaporkan", "Pelapor", "Alasan");
                for (BarangRow r : adminBarangService.arsip(q, idKategori, jenis)) {
                    baris.add(list(r.kode(), r.nama(), r.hilang() ? "Barang hilang" : "Barang temuan",
                            r.kategori(), r.lokasi(), r.dilaporkan(), r.pelapor(),
                            r.alasan()));
                }
                tambahFilterBarang(filter, q, idKategori, jenis, "");
            }
            default -> {
                return ResponseEntity.notFound().build();
            }
        }

        List<String> keterangan = new ArrayList<>();
        keterangan.add("Diekspor pada " + Fmt.tanggalWaktu(LocalDateTime.now())
                + " oleh " + admin.getNamaLengkap());
        keterangan.add("Filter: " + (filter.isEmpty() ? "Semua data" : String.join("; ", filter)));
        keterangan.add("Jumlah baris: " + baris.size());

        byte[] isi = excelExportService.buat(sheet, judul, keterangan, header, baris);
        String namaFile = "findup-" + layar + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ExcelExportService.CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + namaFile + "\"")
                .header("Cache-Control", "no-store")
                .body(isi);
    }

    private void tambahFilterBarang(List<String> filter, String q, int idKategori,
                                    String jenis, String status) {
        if (!q.isBlank()) {
            filter.add("Pencarian: " + q);
        }
        if (idKategori != 0) {
            kategoriService.semua().stream()
                    .filter(k -> k.getIdKategori() == idKategori)
                    .findFirst()
                    .ifPresent(k -> filter.add("Kategori: " + k.getNamaKategori()));
        }
        if (!jenis.isBlank()) {
            filter.add("Jenis: " + jenis);
        }
        if (!status.isBlank()) {
            filter.add("Status: " + status);
        }
    }

    private static String labelStatusAntrean(String st) {
        var s = InteraksiViewService.dariKode(st);
        return s == null ? "Semua" : InteraksiViewService.labelStatus(s);
    }

    private static List<Object> list(Object... nilai) {
        List<Object> hasil = new ArrayList<>();
        for (Object o : nilai) {
            hasil.add(o);
        }
        return hasil;
    }

    private static String normJenis(String jenis) {
        String j = BarangQueryService.normalisasiJenis(jenis);
        return j == null ? "" : j.toLowerCase();
    }
}
