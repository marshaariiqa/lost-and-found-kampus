package lostfound.service;

import org.springframework.stereotype.Component;

import lostfound.dto.KlaimAtauTanggapanResponseDTO;
import lostfound.model.KlaimAtauTanggapan;

@Component
public class KlaimAtauTanggapanMapper {

    public KlaimAtauTanggapanResponseDTO toDTO(
            KlaimAtauTanggapan interaksi) {

        KlaimAtauTanggapanResponseDTO dto =
                new KlaimAtauTanggapanResponseDTO();

        dto.setIdInteraksi(interaksi.getIdInteraksi());

        if (interaksi.getKehilanganBarang() != null) {
            dto.setIdKehilangan(
                    interaksi.getKehilanganBarang().getIdKehilangan());
        }

        if (interaksi.getPenemuBarang() != null) {
            dto.setIdPenemu(
                    interaksi.getPenemuBarang().getIdPenemu());
        }

        dto.setIdPenggunaMerespons(
                interaksi.getPenggunaMerespons().getIdPengguna());

        dto.setNamaPenggunaMerespons(
                interaksi.getPenggunaMerespons().getNamaLengkap());

        dto.setCatatanBukti(interaksi.getCatatanBukti());
        dto.setFotoKtm(interaksi.getFotoKtm());
        dto.setAlasanPenolakan(interaksi.getAlasanPenolakan());
        dto.setStatusInteraksi(interaksi.getStatusInteraksi().name());
        dto.setWaktuDibuat(interaksi.getWaktuDibuat());
        dto.setWaktuVerifikasi(interaksi.getWaktuVerifikasi());

        return dto;
    }
}