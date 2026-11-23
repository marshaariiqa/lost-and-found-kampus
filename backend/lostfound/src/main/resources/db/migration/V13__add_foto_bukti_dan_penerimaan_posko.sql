-- Foto bukti pendukung (opsional) pada klaim/tanggapan, selain foto KTM yang wajib
ALTER TABLE klaim_atau_tanggapan
ADD COLUMN foto_bukti VARCHAR(255) NULL;

-- Waktu barang temuan benar-benar diterima di Posko.
-- NULL = laporan temuan masih di tangan pelapor, belum masuk inventaris Posko.
ALTER TABLE penemu_barang
ADD COLUMN waktu_diterima DATETIME NULL;
