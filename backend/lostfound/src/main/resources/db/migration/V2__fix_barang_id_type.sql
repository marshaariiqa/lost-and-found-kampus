-- 1. Hapus enam foreign key yang mengacu ke tabel barang
ALTER TABLE klaim_atau_tanggapan
    DROP FOREIGN KEY fk_interaksi_kehilangan,
    DROP FOREIGN KEY fk_interaksi_penemu;

ALTER TABLE log_aktivitas
    DROP FOREIGN KEY fk_log_kehilangan,
    DROP FOREIGN KEY fk_log_penemu;

ALTER TABLE notifikasi
    DROP FOREIGN KEY fk_notifikasi_kehilangan,
    DROP FOREIGN KEY fk_notifikasi_penemu;


-- 2. Ubah primary key menjadi BIGINT
ALTER TABLE kehilangan_barang
    MODIFY COLUMN id_kehilangan BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE penemu_barang
    MODIFY COLUMN id_penemu BIGINT NOT NULL AUTO_INCREMENT;


-- 3. Samakan tipe foreign key menjadi BIGINT
ALTER TABLE klaim_atau_tanggapan
    MODIFY COLUMN id_kehilangan BIGINT NULL,
    MODIFY COLUMN id_penemu BIGINT NULL;

ALTER TABLE log_aktivitas
    MODIFY COLUMN id_kehilangan BIGINT NULL,
    MODIFY COLUMN id_penemu BIGINT NULL;

ALTER TABLE notifikasi
    MODIFY COLUMN id_kehilangan BIGINT NULL,
    MODIFY COLUMN id_penemu BIGINT NULL;


-- 4. Buat kembali foreign key klaim atau tanggapan
ALTER TABLE klaim_atau_tanggapan
    ADD CONSTRAINT fk_interaksi_kehilangan
    FOREIGN KEY (id_kehilangan)
    REFERENCES kehilangan_barang(id_kehilangan)
    ON UPDATE CASCADE ON DELETE RESTRICT,

    ADD CONSTRAINT fk_interaksi_penemu
    FOREIGN KEY (id_penemu)
    REFERENCES penemu_barang(id_penemu)
    ON UPDATE CASCADE ON DELETE RESTRICT;


-- 5. Buat kembali foreign key log aktivitas
ALTER TABLE log_aktivitas
    ADD CONSTRAINT fk_log_kehilangan
    FOREIGN KEY (id_kehilangan)
    REFERENCES kehilangan_barang(id_kehilangan)
    ON UPDATE CASCADE ON DELETE RESTRICT,

    ADD CONSTRAINT fk_log_penemu
    FOREIGN KEY (id_penemu)
    REFERENCES penemu_barang(id_penemu)
    ON UPDATE CASCADE ON DELETE RESTRICT;


-- 6. Buat kembali foreign key notifikasi
ALTER TABLE notifikasi
    ADD CONSTRAINT fk_notifikasi_kehilangan
    FOREIGN KEY (id_kehilangan)
    REFERENCES kehilangan_barang(id_kehilangan)
    ON UPDATE CASCADE ON DELETE SET NULL,

    ADD CONSTRAINT fk_notifikasi_penemu
    FOREIGN KEY (id_penemu)
    REFERENCES penemu_barang(id_penemu)
    ON UPDATE CASCADE ON DELETE SET NULL;