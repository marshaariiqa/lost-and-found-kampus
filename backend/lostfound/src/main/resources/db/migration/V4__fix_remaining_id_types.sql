-- Menyesuaikan primary key log_aktivitas
ALTER TABLE log_aktivitas
MODIFY COLUMN id_log BIGINT NOT NULL AUTO_INCREMENT;

-- Menyesuaikan primary key notifikasi
ALTER TABLE notifikasi
MODIFY COLUMN id_notifikasi BIGINT NOT NULL AUTO_INCREMENT;