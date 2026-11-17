package lostfound.service;

import lostfound.model.KehilanganBarang;
import lostfound.model.PenemuBarang;
import lostfound.model.StatusBarang;
import lostfound.repository.KehilanganBarangRepository;
import lostfound.repository.PenemuBarangRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArsipBarangService {

    @Autowired
    private KehilanganBarangRepository kehilanganBarangRepository;

    @Autowired
    private PenemuBarangRepository penemuBarangRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void arsipkanLaporanKedaluwarsa() {

        LocalDateTime batasWaktu = LocalDateTime.now().minusDays(7);

        List<KehilanganBarang> laporanKehilangan =
                kehilanganBarangRepository.findByStatusAndWaktuDibuatBefore(
                        StatusBarang.TERSEDIA, batasWaktu);

        for (KehilanganBarang barang : laporanKehilangan) {
            barang.setStatus(StatusBarang.DIARSIPKAN);
        }

        kehilanganBarangRepository.saveAll(laporanKehilangan);

        List<PenemuBarang> laporanTemuan =
                penemuBarangRepository.findByStatusAndWaktuDibuatBefore(
                        StatusBarang.TERSEDIA, batasWaktu);

        for (PenemuBarang barang : laporanTemuan) {
            barang.setStatus(StatusBarang.DIARSIPKAN);
        }

        penemuBarangRepository.saveAll(laporanTemuan);
    }
}