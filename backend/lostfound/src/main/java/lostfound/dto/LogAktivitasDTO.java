package lostfound.dto;

import java.time.LocalDateTime;

/**
 * Representasi aman log aktivitas (tanpa data akun admin seperti kata sandi).
 */
public record LogAktivitasDTO(Long idLog, Long idKehilangan, Long idPenemu,
                              Integer idAdmin, String namaAdmin,
                              String tindakan, LocalDateTime waktuCatat) {
}
