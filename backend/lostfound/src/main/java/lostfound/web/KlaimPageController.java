package lostfound.web;

import lostfound.model.KlaimAtauTanggapan;
import lostfound.model.Pengguna;
import lostfound.service.BarangQueryService;
import lostfound.service.CurrentUserService;
import lostfound.service.FileStorageService;
import lostfound.service.InteraksiViewService;
import lostfound.service.KlaimAtauTanggapanService;
import lostfound.util.Fmt;
import lostfound.view.BarangDetailView;

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
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Status klaim/tanggapan milik mahasiswa dan form pengajuannya.
 *
 * - Barang temuan  -> "Ajukan Klaim"       (ciri-ciri barang + bukti)
 * - Barang hilang  -> "Saya Menemukan Ini" (lokasi, tanggal, kondisi barang yang ditemukan)
 * Foto KTM WAJIB di keduanya (divalidasi di sini, di FileStorageService, dan di service klaim).
 * Foto bukti/foto barang bersifat opsional.
 */
@Controller
public class KlaimPageController {

    /** Isian form agar bisa ditampilkan kembali bila ada kesalahan. */
    public record KlaimForm(String catatan, String lokasi, String tanggal, String kondisi, boolean setuju) {
    }

    private final CurrentUserService currentUserService;
    private final InteraksiViewService interaksiViewService;
    private final BarangQueryService barangQueryService;
    private final KlaimAtauTanggapanService klaimService;
    private final FileStorageService fileStorageService;

    public KlaimPageController(CurrentUserService currentUserService,
                               InteraksiViewService interaksiViewService,
                               BarangQueryService barangQueryService,
                               KlaimAtauTanggapanService klaimService,
                               FileStorageService fileStorageService) {
        this.currentUserService = currentUserService;
        this.interaksiViewService = interaksiViewService;
        this.barangQueryService = barangQueryService;
        this.klaimService = klaimService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/klaim")
    public String daftar(@RequestParam(defaultValue = "") String status,
                         Authentication authentication, Model model) {
        Pengguna saya = currentUserService.wajib(authentication);
        var semua = interaksiViewService.milikSaya(saya.getIdPengguna(), "");
        var tampil = interaksiViewService.milikSaya(saya.getIdPengguna(), status);

        model.addAttribute("rows", tampil);
        model.addAttribute("total", semua.size());
        model.addAttribute("nMenunggu", semua.stream().filter(r -> r.menunggu()).count());
        model.addAttribute("nDisetujui", semua.stream().filter(r -> r.disetujui()).count());
        model.addAttribute("nSelesai", semua.stream().filter(r -> r.selesai()).count());
        model.addAttribute("nDitolak", semua.stream().filter(r -> r.ditolak()).count());
        model.addAttribute("statusSel", status.toLowerCase());
        return "mhs/klaim-list";
    }

    @GetMapping("/klaim/{id}")
    public String status(@PathVariable Long id, Authentication authentication,
                         Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        Optional<InteraksiViewService.KlaimDetail> detail =
                interaksiViewService.detailPemohon(id, saya.getIdPengguna());
        if (detail.isEmpty()) {
            redirect.addFlashAttribute("toastError", "Pengajuan tidak ditemukan.");
            return "redirect:/klaim";
        }
        model.addAttribute("k", detail.get());
        return "mhs/klaim-status";
    }

    /** Halaman konfirmasi setelah pengajuan berhasil dikirim. */
    @GetMapping("/klaim/{id}/terkirim")
    public String terkirim(@PathVariable Long id, Authentication authentication,
                           Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        Optional<InteraksiViewService.KlaimDetail> detail =
                interaksiViewService.detailPemohon(id, saya.getIdPengguna());
        if (detail.isEmpty()) {
            redirect.addFlashAttribute("toastError", "Pengajuan tidak ditemukan.");
            return "redirect:/klaim";
        }
        model.addAttribute("k", detail.get());
        return "mhs/klaim-terkirim";
    }

    @GetMapping("/klaim/baru/{jenis}/{id}")
    public String form(@PathVariable String jenis, @PathVariable Long id,
                       Authentication authentication, Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        BarangDetailView d = ambilBisaKlaim(jenis, id, saya, redirect);
        if (d == null) {
            return "redirect:/barang/" + jenis + "/" + id;
        }
        isiModel(model, d, new KlaimForm("", "", "", "", false), new LinkedHashMap<>());
        return "mhs/klaim-form";
    }

    @PostMapping("/klaim/baru/{jenis}/{id}")
    public String kirim(@PathVariable String jenis, @PathVariable Long id,
                        @RequestParam(defaultValue = "") String catatan,
                        @RequestParam(defaultValue = "") String lokasi,
                        @RequestParam(defaultValue = "") String tanggal,
                        @RequestParam(defaultValue = "") String kondisi,
                        @RequestParam(required = false) String pernyataan,
                        @RequestParam(name = "fotoKtm", required = false) MultipartFile fotoKtm,
                        @RequestParam(name = "fotoBukti", required = false) MultipartFile fotoBukti,
                        Authentication authentication, Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        BarangDetailView d = ambilBisaKlaim(jenis, id, saya, redirect);
        if (d == null) {
            return "redirect:/barang/" + jenis + "/" + id;
        }

        KlaimForm form = new KlaimForm(catatan.trim(), lokasi.trim(), tanggal.trim(),
                kondisi.trim(), pernyataan != null);
        Map<String, String> errors = new LinkedHashMap<>();
        String bukti = "";

        if (d.hilang()) {
            // "Saya Menemukan Ini": rincian temuan disusun menjadi satu catatan bukti
            if (form.lokasi().isEmpty()) {
                errors.put("lokasi", "Lokasi ditemukan wajib diisi");
            } else if (form.lokasi().length() > 255) {
                errors.put("lokasi", "Lokasi maksimal 255 karakter");
            }
            LocalDate tgl = null;
            if (form.tanggal().isEmpty()) {
                errors.put("tanggal", "Tanggal ditemukan wajib diisi");
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
            if (form.kondisi().isEmpty()) {
                errors.put("kondisi", "Jelaskan kondisi atau ciri barang yang kamu temukan");
            }
            if (errors.isEmpty()) {
                bukti = "Lokasi ditemukan: " + form.lokasi()
                        + "\nTanggal ditemukan: " + Fmt.tanggal(tgl.atStartOfDay())
                        + "\nKondisi/ciri barang: " + form.kondisi();
            }
        } else {
            if (form.catatan().isEmpty()) {
                errors.put("catatan", "Jelaskan ciri-ciri barang yang hanya pemiliknya yang tahu");
            }
            bukti = form.catatan();
        }

        if (fotoKtm == null || fotoKtm.isEmpty()) {
            errors.put("fotoKtm", "Foto KTM wajib diunggah");
        }
        if (!form.setuju()) {
            errors.put("pernyataan", "Silakan centang persetujuan terlebih dahulu sebelum mengirim pengajuan.");
        }

        String pathKtm = null;
        String pathBukti = null;
        if (errors.isEmpty()) {
            try {
                pathKtm = fileStorageService.simpanFotoKtm(fotoKtm);
            } catch (IllegalArgumentException e) {
                errors.put("fotoKtm", e.getMessage());
            }
        }
        if (errors.isEmpty() && fotoBukti != null && !fotoBukti.isEmpty()) {
            try {
                pathBukti = fileStorageService.simpanFotoBukti(fotoBukti);
            } catch (IllegalArgumentException e) {
                errors.put("fotoBukti", e.getMessage());
                fileStorageService.hapus(pathKtm);
                pathKtm = null;
            }
        }

        if (!errors.isEmpty()) {
            isiModel(model, d, form, errors);
            model.addAttribute("toastError", "Periksa kembali kolom yang ditandai.");
            return "mhs/klaim-form";
        }

        try {
            KlaimAtauTanggapan hasil = d.hilang()
                    ? klaimService.ajukanTanggapanKehilangan(id, bukti, pathKtm, authentication)
                    : klaimService.ajukanKlaimTemuan(id, bukti, pathKtm, authentication);
            if (pathBukti != null) {
                try {
                    klaimService.simpanFotoBukti(hasil.getIdInteraksi(), pathBukti);
                } catch (RuntimeException e) {
                    // Foto bukti opsional: kegagalan menyimpannya tidak membatalkan pengajuan
                    fileStorageService.hapus(pathBukti);
                }
            }
            redirect.addFlashAttribute("toastSukses",
                    d.hilang() ? "Tanggapan berhasil dikirim." : "Klaim berhasil diajukan.");
            return "redirect:/klaim/" + hasil.getIdInteraksi() + "/terkirim";
        } catch (RuntimeException e) {
            fileStorageService.hapus(pathKtm);
            fileStorageService.hapus(pathBukti);
            isiModel(model, d, form, errors);
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "mhs/klaim-form";
        }
    }

    private BarangDetailView ambilBisaKlaim(String jenis, Long id, Pengguna saya,
                                            RedirectAttributes redirect) {
        Optional<BarangDetailView> detail = barangQueryService.detail(jenis, id, saya);
        if (detail.isEmpty()) {
            redirect.addFlashAttribute("toastError", "Laporan tidak ditemukan.");
            return null;
        }
        BarangDetailView d = detail.get();
        if (!d.bisaKlaim()) {
            String alasan = d.milikSaya()
                    ? "Kamu tidak dapat mengajukan klaim pada laporanmu sendiri."
                    : !d.tersedia()
                            ? "Laporan ini sudah selesai diproses."
                            : "Kamu sudah memiliki pengajuan yang sedang diproses untuk barang ini.";
            redirect.addFlashAttribute("toastError", alasan);
            return null;
        }
        return d;
    }

    private void isiModel(Model model, BarangDetailView d, KlaimForm form, Map<String, String> errors) {
        model.addAttribute("d", d);
        model.addAttribute("f", form);
        model.addAttribute("errors", errors);
        model.addAttribute("hariIni", LocalDate.now().toString());
    }
}
