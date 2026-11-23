package lostfound.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pembuat file Excel asli (.xlsx, format OOXML) memakai Apache POI.
 * Data yang diekspor selalu berasal dari daftar yang sama dengan tampilan
 * halaman admin, sehingga mengikuti filter yang sedang aktif.
 */
@Service
public class ExcelExportService {

    public static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * @param namaSheet nama sheet (maks 31 karakter, tanpa karakter terlarang)
     * @param judul     judul laporan di baris pertama
     * @param keterangan baris keterangan (waktu ekspor, filter aktif, jumlah baris)
     * @param header    nama kolom
     * @param baris     isi data: String, Number, LocalDateTime, atau null
     */
    public byte[] buat(String namaSheet, String judul, List<String> keterangan,
                       List<String> header, List<List<Object>> baris) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(amanNamaSheet(namaSheet));

            XSSFFont fontJudul = wb.createFont();
            fontJudul.setBold(true);
            fontJudul.setFontHeightInPoints((short) 14);

            XSSFFont fontHeader = wb.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(new XSSFColor(new byte[] {(byte) 255, (byte) 255, (byte) 255}, null));

            XSSFFont fontKet = wb.createFont();
            fontKet.setColor(new XSSFColor(new byte[] {(byte) 107, (byte) 114, (byte) 128}, null));

            CellStyle gayaJudul = wb.createCellStyle();
            gayaJudul.setFont(fontJudul);

            CellStyle gayaKet = wb.createCellStyle();
            gayaKet.setFont(fontKet);

            XSSFCellStyle gayaHeader = wb.createCellStyle();
            gayaHeader.setFont(fontHeader);
            gayaHeader.setFillForegroundColor(
                    new XSSFColor(new byte[] {(byte) 219, (byte) 39, (byte) 119}, null));
            gayaHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            gayaHeader.setAlignment(HorizontalAlignment.LEFT);
            gayaHeader.setBorderBottom(BorderStyle.THIN);

            CellStyle gayaTeks = wb.createCellStyle();
            gayaTeks.setWrapText(false);

            CellStyle gayaTanggal = wb.createCellStyle();
            gayaTanggal.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd mmm yyyy hh:mm"));
            gayaTanggal.setAlignment(HorizontalAlignment.LEFT);

            int r = 0;
            Row rowJudul = sheet.createRow(r++);
            Cell cJudul = rowJudul.createCell(0);
            cJudul.setCellValue(judul);
            cJudul.setCellStyle(gayaJudul);

            for (String k : keterangan) {
                Row rowKet = sheet.createRow(r++);
                Cell cKet = rowKet.createCell(0);
                cKet.setCellValue(k);
                cKet.setCellStyle(gayaKet);
            }
            r++; // baris kosong

            int barisHeader = r;
            Row rowHeader = sheet.createRow(r++);
            int[] lebar = new int[header.size()];
            for (int c = 0; c < header.size(); c++) {
                Cell cell = rowHeader.createCell(c);
                cell.setCellValue(header.get(c));
                cell.setCellStyle(gayaHeader);
                lebar[c] = header.get(c).length();
            }

            for (List<Object> data : baris) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < header.size(); c++) {
                    Object nilai = c < data.size() ? data.get(c) : null;
                    Cell cell = row.createCell(c);
                    if (nilai instanceof LocalDateTime t) {
                        cell.setCellValue(t);
                        cell.setCellStyle(gayaTanggal);
                        lebar[c] = Math.max(lebar[c], 17);
                    } else if (nilai instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                        lebar[c] = Math.max(lebar[c], String.valueOf(n).length());
                    } else {
                        String teks = nilai == null ? "" : nilai.toString();
                        cell.setCellValue(teks);
                        cell.setCellStyle(gayaTeks);
                        lebar[c] = Math.max(lebar[c], teks.length());
                    }
                }
            }

            // Lebar kolom dihitung manual (autoSizeColumn butuh font sistem/AWT)
            for (int c = 0; c < header.size(); c++) {
                int karakter = Math.min(60, Math.max(10, lebar[c] + 2));
                sheet.setColumnWidth(c, karakter * 256);
            }

            if (!header.isEmpty()) {
                int barisTerakhir = Math.max(barisHeader, r - 1);
                sheet.setAutoFilter(new CellRangeAddress(barisHeader, barisTerakhir, 0, header.size() - 1));
                sheet.createFreezePane(0, barisHeader + 1);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat file Excel", e);
        }
    }

    private static String amanNamaSheet(String nama) {
        String bersih = nama == null ? "Data" : nama.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (bersih.isEmpty()) {
            bersih = "Data";
        }
        return bersih.length() > 31 ? bersih.substring(0, 31) : bersih;
    }
}
