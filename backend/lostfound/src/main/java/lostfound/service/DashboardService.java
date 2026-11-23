package lostfound.service;

import lostfound.model.LogAktivitas;
import lostfound.model.StatusBarang;
import lostfound.model.StatusInteraksi;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.KlaimAtauTanggapanRepository;
import lostfound.repository.LogAktivitasRepository;
import lostfound.repository.PenemuBarangRepository;
import lostfound.repository.PenggunaRepository;
import lostfound.service.InteraksiViewService.KlaimRow;
import lostfound.util.Fmt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Angka ringkasan dashboard. Semua nilai dihitung dari database.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    public record Mahasiswa(long laporanAktif, long hilangAktif, long temuanAktif,
                            long menunggu, long disetujui,
                            long laporanSelesai, long pengajuanSelesai) {

        public long selesai() {
            return laporanSelesai + pengajuanSelesai;
        }
    }

    public record Aktivitas(String waktu, String admin, String tindakan) {
    }

    public record Admin(long menunggu, long diPosko, long menungguTerima, long diserahkanBulanIni,
                        long diserahkanBulanLalu, long penggunaAktif,
                        List<KlaimRow> antreanTerbaru, List<Aktivitas> aktivitas) {
    }

    private final KehilanganBarangRepository kehilanganRepository;
    private final PenemuBarangRepository penemuRepository;
    private final KlaimAtauTanggapanRepository klaimRepository;
    private final PenggunaRepository penggunaRepository;
    private final LogAktivitasRepository logRepository;
    private final InteraksiViewService interaksiViewService;

    public DashboardService(KehilanganBarangRepository kehilanganRepository,
                            PenemuBarangRepository penemuRepository,
                            KlaimAtauTanggapanRepository klaimRepository,
                            PenggunaRepository penggunaRepository,
                            LogAktivitasRepository logRepository,
                            InteraksiViewService interaksiViewService) {
        this.kehilanganRepository = kehilanganRepository;
        this.penemuRepository = penemuRepository;
        this.klaimRepository = klaimRepository;
        this.penggunaRepository = penggunaRepository;
        this.logRepository = logRepository;
        this.interaksiViewService = interaksiViewService;
    }

    public Mahasiswa mahasiswa(Integer idPengguna) {
        long hilang = kehilanganRepository
                .countByPengguna_IdPenggunaAndStatusAndDihapusFalse(idPengguna, StatusBarang.TERSEDIA);
        long temuan = penemuRepository
                .countByPengguna_IdPenggunaAndStatusAndDihapusFalse(idPengguna, StatusBarang.TERSEDIA);

        long laporanSelesai = kehilanganRepository
                .countByPengguna_IdPenggunaAndStatusAndDihapusFalse(idPengguna, StatusBarang.SELESAI)
                + penemuRepository
                .countByPengguna_IdPenggunaAndStatusAndDihapusFalse(idPengguna, StatusBarang.SELESAI);
        long interaksiSelesai = klaimRepository
                .countByPenggunaMerespons_IdPenggunaAndStatusInteraksi(idPengguna, StatusInteraksi.SELESAI);

        return new Mahasiswa(
                hilang + temuan, hilang, temuan,
                klaimRepository.countByPenggunaMerespons_IdPenggunaAndStatusInteraksi(
                        idPengguna, StatusInteraksi.MENUNGGU_VERIFIKASI),
                klaimRepository.countByPenggunaMerespons_IdPenggunaAndStatusInteraksi(
                        idPengguna, StatusInteraksi.DISETUJUI),
                laporanSelesai, interaksiSelesai);
    }

    public Admin admin() {
        LocalDateTime awalBulanIni = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime awalBulanLalu = awalBulanIni.minusMonths(1);

        long bulanIni = klaimRepository.countByWaktuSelesaiBetween(
                awalBulanIni, awalBulanIni.plusMonths(1).minusSeconds(1));
        long bulanLalu = klaimRepository.countByWaktuSelesaiBetween(
                awalBulanLalu, awalBulanIni.minusSeconds(1));

        List<KlaimRow> antrean = interaksiViewService.antrean("wait", "");
        // antrean terlama menunggu paling atas agar segera diproses
        List<KlaimRow> terbaru = new ArrayList<>(
                antrean.size() > 3 ? antrean.subList(0, 3) : antrean);

        List<Aktivitas> aktivitas = new ArrayList<>();
        for (LogAktivitas log : logRepository.findTop6ByOrderByWaktuCatatDesc()) {
            aktivitas.add(new Aktivitas(
                    Fmt.tanggalSingkat(log.getWaktuCatat()),
                    log.getAdmin().getNamaLengkap(),
                    log.getTindakan()));
        }

        return new Admin(
                klaimRepository.countByStatusInteraksi(StatusInteraksi.MENUNGGU_VERIFIKASI),
                penemuRepository.countByStatusAndDihapusFalseAndWaktuDiterimaIsNotNull(StatusBarang.TERSEDIA),
                penemuRepository.countByStatusAndDihapusFalseAndWaktuDiterimaIsNull(StatusBarang.TERSEDIA),
                bulanIni, bulanLalu,
                penggunaRepository.countByAktifTrue(),
                terbaru, aktivitas);
    }
}
