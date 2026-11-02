-- 1. Lepas foreign key notifikasi sementara
ALTER TABLE notifikasi
DROP FOREIGN KEY fk_notifikasi_interaksi;

-- 2. Ubah primary key menjadi BIGINT
ALTER TABLE klaim_atau_tanggapan
MODIFY COLUMN id_interaksi BIGINT NOT NULL AUTO_INCREMENT;

-- 3. Samakan tipe foreign key di notifikasi
ALTER TABLE notifikasi
MODIFY COLUMN id_interaksi BIGINT NULL;

-- 4. Pasang kembali foreign key
ALTER TABLE notifikasi
ADD CONSTRAINT fk_notifikasi_interaksi
FOREIGN KEY (id_interaksi)
REFERENCES klaim_atau_tanggapan(id_interaksi)
ON UPDATE CASCADE
ON DELETE SET NULL;