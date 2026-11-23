package lostfound.web;

import lostfound.dto.KehilanganBarangDTO;
import lostfound.dto.PenemuBarangDTO;
import lostfound.model.Pengguna;
import lostfound.service.BarangQueryService;
import lostfound.service.BarangQueryService.BarangEdit;
import lostfound.service.CurrentUserService;
import lostfound.service.FileStorageService;
import lostfound.service.KategoriService;
import lostfound.service.KehilanganBarangService;
import lostfound.service.PenemuBarangService;
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
 * Edit laporan milik sendiri. Hanya laporan TERSEDIA milik pengguna yang login.
 * Kepemilikan dan status DIVALIDASI ULANG di service (updateKehilangan/updateTemuan),
 * jadi permintaan langsung ke endpoint tetap ditolak walau tombolnya disembunyikan.
 * (Hapus ada di LaporanPageController dan dijaga service yang sama.)
 */
@Controller
public class LaporanEditController {

    public record EditForm(String nama, String idKategori, String deskripsi, String ciri,
                           String lokasi, String tanggal) {
    }

    private final CurrentUserService currentUserService;
    private final BarangQueryService barangQueryService;
    private final KategoriService kategoriService;
    private final KehilanganBarangService kehilanganService;
    private final PenemuBarangService penemuService;
    private final FileStorageService fileStorageService;

    public LaporanEditController(CurrentUserService currentUserService,
                                 BarangQueryService barangQueryService,
                                 KategoriService kategoriService,
                                 KehilanganBarangService kehilanganService,
                                 PenemuBarangService penemuService,
                                 FileStorageService fileStorageService) {
        this.currentUserService = currentUserService;
        this.barangQueryService = barangQueryService;
        this.kategoriService = kategoriService;
        this.kehilanganService = kehilanganService;
        this.penemuService = penemuService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/laporan/{jenis}/{id}/edit")
    public String form(@PathVariable String jenis, @PathVariable Long id,
                       Authentication authentication, Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        String tolak = alasanTolak(barangQueryService.detail(jenis, id, saya));
        Optional<BarangEdit> data = barangQueryService.dataEdit(jenis, id, saya);
        if (tolak != null || data.isEmpty()) {
            redirect.addFlashAttribute("toastError", tolak != null ? tolak : "Laporan tidak ditemukan.");
            return "redirect:/laporan";
        }
        BarangEdit e = data.get();
        isiModel(model, e, new EditForm(e.nama(), String.valueOf(e.idKategori()),
                e.deskripsi() == null ? "" : e.deskripsi(), e.ciri() == null ? "" : e.ciri(),
                e.lokasi(), e.tanggal()), new LinkedHashMap<>());
        return "mhs/laporan-edit";
    }

    @PostMapping("/laporan/{jenis}/{id}/edit")
    public String simpan(@PathVariable String jenis, @PathVariable Long id,
                         @RequestParam(defaultValue = "") String nama,
                         @RequestParam(defaultValue = "") String idKategori,
                         @RequestParam(defaultValue = "") String deskripsi,
                         @RequestParam(defaultValue = "") String ciri,
                         @RequestParam(defaultValue = "") String lokasi,
                         @RequestParam(defaultValue = "") String tanggal,
                         @RequestParam(name = "foto", required = false) MultipartFile foto,
                         Authentication authentication, Model model, RedirectAttributes redirect) {
        Pengguna saya = currentUserService.wajib(authentication);
        String tolak = alasanTolak(barangQueryService.detail(jenis, id, saya));
        Optional<BarangEdit> data = barangQueryService.dataEdit(jenis, id, saya);
        if (tolak != null || data.isEmpty()) {
            redirect.addFlashAttribute("toastError", tolak != null ? tolak : "Laporan tidak ditemukan.");
            return "redirect:/laporan";
        }
        BarangEdit lama = data.get();
        boolean hilang = BarangQueryService.HILANG.equals(lama.jenis());

        EditForm form = new EditForm(nama.trim(), idKategori, deskripsi.trim(), ciri.trim(),
                lokasi.trim(), tanggal.trim());
        Map<String, String> errors = new LinkedHashMap<>();
        int kategoriId = WebSupport.angka(idKategori);

        if (form.nama().isEmpty()) {
            errors.put("nama", "Nama barang wajib diisi");
        } else if (form.nama().length() > 150) {
            errors.put("nama", "Nama barang maksimal 150 karakter");
        }
        if (kategoriId == 0) {
            errors.put("idKategori", "Kategori wajib dipilih");
        }
        if (form.deskripsi().isEmpty()) {
            errors.put("deskripsi", hilang ? "Deskripsi publik wajib diisi" : "Deskripsi barang wajib diisi");
        }
        if (form.lokasi().isEmpty()) {
            errors.put("lokasi", hilang ? "Lokasi kejadian wajib diisi" : "Lokasi ditemukan wajib diisi");
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

        String urlBaru = null;
        if (errors.isEmpty() && foto != null && !foto.isEmpty()) {
            try {
                urlBaru = fileStorageService.simpanFotoBarang(foto);
            } catch (IllegalArgumentException e) {
                errors.put("foto", e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            isiModel(model, lama, form, errors);
            model.addAttribute("toastError", "Periksa kembali kolom yang ditandai.");
            return "mhs/laporan-edit";
        }

        String urlAkhir = urlBaru != null ? urlBaru : lama.urlGambar();
        try {
            if (hilang) {
                KehilanganBarangDTO dto = new KehilanganBarangDTO();
                dto.setIdKategori(kategoriId);
                dto.setNamaBarang(form.nama());
                dto.setDeskripsiPublik(form.deskripsi());
                dto.setSpesifikasiRahasia(form.ciri().isEmpty() ? null : form.ciri());
                dto.setLokasiKejadian(form.lokasi());
                dto.setTanggalKejadian(tgl.atStartOfDay());
                dto.setUrlGambar(urlAkhir);
                kehilanganService.updateKehilangan(id, dto, authentication.getName());
            } else {
                PenemuBarangDTO dto = new PenemuBarangDTO();
                dto.setIdKategori(kategoriId);
                dto.setNamaBarang(form.nama());
                dto.setDeskripsi(form.deskripsi());
                dto.setLokasiDitemukan(form.lokasi());
                dto.setTanggalDitemukan(tgl.atStartOfDay());
                dto.setUrlGambar(urlAkhir);
                penemuService.updateTemuan(id, dto, authentication.getName());
            }
            if (urlBaru != null) {
                fileStorageService.hapus(lama.urlGambar());   // foto lama sudah tidak dipakai
            }
            redirect.addFlashAttribute("toastSukses", "Laporan berhasil diperbarui.");
            return "redirect:/barang/" + (hilang ? "hilang" : "temuan") + "/" + id;
        } catch (RuntimeException e) {
            fileStorageService.hapus(urlBaru);
            isiModel(model, lama, form, errors);
            model.addAttribute("toastError", WebSupport.pesan(e));
            return "mhs/laporan-edit";
        }
    }

    /** Pesan penolakan bila pengguna tidak boleh mengubah laporan ini; null bila boleh. */
    private String alasanTolak(Optional<BarangDetailView> detail) {
        if (detail.isEmpty()) {
            return "Laporan tidak ditemukan.";
        }
        BarangDetailView d = detail.get();
        if (!d.milikSaya()) {
            return "Kamu tidak memiliki akses untuk mengubah laporan ini.";
        }
        if (!d.tersedia()) {
            return "Laporan hanya dapat diubah jika masih tersedia.";
        }
        return null;
    }

    private void isiModel(Model model, BarangEdit data, EditForm form, Map<String, String> errors) {
        model.addAttribute("e", data);
        model.addAttribute("f", form);
        model.addAttribute("errors", errors);
        model.addAttribute("kategoriList", kategoriService.semua());
        model.addAttribute("hariIni", LocalDate.now().toString());
    }
}
