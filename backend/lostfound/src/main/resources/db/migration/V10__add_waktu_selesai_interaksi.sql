-- Waktu penyerahan barang dikonfirmasi admin (untuk riwayat penyerahan & statistik)
ALTER TABLE klaim_atau_tanggapan
ADD COLUMN waktu_selesai DATETIME NULL;
