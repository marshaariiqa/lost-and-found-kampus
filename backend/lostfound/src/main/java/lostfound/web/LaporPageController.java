package lostfound.web;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.PenemuBarangDTO;
import lostfound.service.BarangQueryService;
import lostfound.service.FileStorageService;
import lostfound.service.KategoriService;
import lostfound.service.KehilanganBarangService;
import lostfound.service.PenemuBarangService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Form lapor barang hilang / temuan (wizard 3 langkah di sisi tampilan,
 * satu kali kirim ke backend).
 */
@Controller
public class LaporPageController {

    public record LaporForm(String jenis, String nama, String idKategori, String deskripsi,
                            String ciri, String lokasi, String tanggal) {
        public boolean hilang() {
            return !"temuan".equals(jenis);
        }
    }

    private final KategoriService kategoriService;
    private final KehilanganBarangService kehilanganService;
    private final PenemuBarangService penemuService;
    private final FileStorageService fileStorageService;

    public LaporPageController(KategoriService kategoriService,
                               KehilanganBarangService kehilanganService,
                               PenemuBarangService penemuService,
                               FileStorageService fileStorageService) {
        this.kategoriService = kategoriService;
        this.kehilanganService = kehilanganService;
        this.penemuService = penemuService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/lapor")
    public String form(@RequestParam(defaultValue = "hilang") String jenis, Model model) {
        String j = "temuan".equalsIgnoreCase(jenis) ? "temuan" : "hilang";
        isiModel(model, new LaporForm(j, "", "", "", "", "", ""), new LinkedHashMap<>());
        return "mhs/lapor";
    }

    @PostMapping("/lapor")
    public String kirim(@RequestParam(defaultValue = "hilang") String jenis,
                        @RequestParam(defaultValue = "") String nama,
                        @RequestParam(defaultValue = "") String idKategori,
                        @RequestParam(defaultValue = "") String deskripsi,
                        @RequestParam(defaultValue = "") String ciri,
                        @RequestParam(defaultValue = "") String lokasi,
                        @RequestParam(defaultValue = "") String tanggal,
                        @RequestParam(name = "foto", required = false) MultipartFile foto,
                        Authentication authentication,
                        Model model,
                        RedirectAttributes redirect) {

        String j = "temuan".equalsIgnoreCase(jenis) ? "temuan" : "hilang";
        LaporForm form = new LaporForm(j, nama.trim(), idKategori, deskripsi.trim(),
                ciri.trim(), lokasi.trim(), tanggal.trim());
        Map<String, String> errors = new LinkedHashMap<>();

        if (form.nama().isEmpty()) {
            errors.put("nama", "Nama barang wajib diisi");
        } else if (form.nama().length() > 150) {
            errors.put("nama", "Nama barang maksimal 150 karakter");
        }
        int kategoriId = WebSupport.angka(idKategori);
        if (kategoriId == 0) {
            errors.put("idKategori", "Kategori wajib dipilih");
        }
        if (form.deskripsi().isEmpty()) {
            errors.put("deskripsi", j.equals("hilang")
                    ? "Deskripsi publik wajib diisi" : "Deskripsi barang wajib diisi");
        }
        if (form.lokasi().isEmpty()) {
            errors.put("lokasi", j.equals("hilang")
                    ? "Lokasi kejadian wajib diisi" : "Lokasi ditemukan wajib diisi");
        } else if (form.lokasi().length() > 255) {
            errors.put("lokasi", "Lokasi maksimal 255 karakter");
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
            isiModel(model, form, errors);
            model.addAttribute("toastError", "Periksa kembali kolom yang ditandai.");
            return "mhs/lapor";
        }

        try {
            if (j.equals("hilang")) {
                KehilanganBarangDTO dto = new KehilanganBarangDTO();
                dto.setIdKategori(kategoriId);
                dto.setNamaBarang(form.nama());
                dto.setDeskripsiPublik(form.deskripsi());
                dto.setSpesifikasiRahasia(form.ciri().isEmpty() ? null : form.ciri());
                dto.setLokasiKejadian(form.lokasi());
                dto.setTanggalKejadian(tgl.atStartOfDay());
                dto.setUrlGambar(urlFoto);
                kehilanganService.createKehilangan(dto, authentication.getName());
                redirect.addFlashAttribute("toastSukses", "Laporan kehilangan berhasil diposting.");
            } else {
                PenemuBarangDTO dto = new PenemuBarangDTO();
                dto.setIdKategori(kategoriId);
                dto.setNamaBarang(form.nama());
                dto.setDeskripsi(form.deskripsi());
                dto.setLokasiDitemukan(form.lokasi());
                dto.setTanggalDitemukan(tgl.atStartOfDay());
                dto.setUrlGambar(urlFoto);
                penemuService.createTemuan(dto, authentication.getName());
                redirect.addFlashAttribute("toastSukses", "Laporan penemuan berhasil diposting.");
            }
            return "redirect:/laporan";
        } catch (RuntimeException e) {
            fileStorageService.hapus(urlFoto);
            isiModel(model, form, errors);
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "mhs/lapor";
        }
    }

    private void isiModel(Model model, LaporForm form, Map<String, String> errors) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("hariIni", LocalDate.now().toString());
    }
}
