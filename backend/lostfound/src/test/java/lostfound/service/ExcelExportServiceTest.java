package lostfound.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ExcelExportServiceTest {

    private final ExcelExportService service = new ExcelExportService();

    @Test
    void menghasilkanFileXlsxAsliYangBisaDibukaKembali() throws Exception {
        List<List<Object>> baris = new ArrayList<>();
        baris.add(new ArrayList<>(List.of("#KLM-00001", "Dompet kulit", 3)));
        List<Object> denganNullDanTanggal = new ArrayList<>();
        denganNullDanTanggal.add("#KLM-00002");
        denganNullDanTanggal.add(null);
        denganNullDanTanggal.add(LocalDateTime.of(2026, 11, 20, 14, 30));
        baris.add(denganNullDanTanggal);

        byte[] hasil = service.buat("Riwayat/Penyerahan?", "FindUp! - Riwayat",
                List.of("Diekspor oleh Admin", "Filter: Semua data"),
                List.of("Kode", "Barang", "Nilai"), baris);

        // Berkas xlsx adalah arsip ZIP: diawali "PK"
        assertThat(hasil).hasSizeGreaterThan(1000);
        assertThat(hasil[0]).isEqualTo((byte) 'P');
        assertThat(hasil[1]).isEqualTo((byte) 'K');

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(hasil))) {
            Sheet sheet = wb.getSheetAt(0);
            // karakter terlarang pada nama sheet diganti spasi
            assertThat(sheet.getSheetName()).doesNotContain("/").doesNotContain("?");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("FindUp! - Riwayat");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).contains("Admin");

            // judul(0) + 2 keterangan(1,2) + baris kosong(3) -> header di baris 4
            Row header = sheet.getRow(4);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Kode");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Nilai");

            Row data1 = sheet.getRow(5);
            assertThat(data1.getCell(1).getStringCellValue()).isEqualTo("Dompet kulit");
            assertThat(data1.getCell(2).getNumericCellValue()).isEqualTo(3.0);

            Row data2 = sheet.getRow(6);
            assertThat(data2.getCell(1).getStringCellValue()).isEmpty();
            // sel tanggal sungguhan (bukan teks); toleransi 1 detik untuk pembulatan serial Excel
            assertThat(DateUtil.isCellDateFormatted(data2.getCell(2))).isTrue();
            long harapan = java.sql.Timestamp.valueOf(LocalDateTime.of(2026, 11, 20, 14, 30)).getTime();
            long aktual = data2.getCell(2).getDateCellValue().getTime();
            assertThat(Math.abs(aktual - harapan)).isLessThan(1000L);
        }
    }

    @Test
    void tetapValidWalauTanpaBaris() throws Exception {
        byte[] hasil = service.buat("Kosong", "Judul", List.of("Jumlah baris: 0"),
                List.of("A", "B"), new ArrayList<>());
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(hasil))) {
            assertThat(wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue()).isEqualTo("A");
        }
    }
}
