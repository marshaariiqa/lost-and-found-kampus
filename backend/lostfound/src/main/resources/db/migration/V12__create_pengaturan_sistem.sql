-- Pengaturan sistem berbentuk key-value. Tidak ada data awal: nilai bawaan ada di kode.
CREATE TABLE pengaturan_sistem (
    kunci VARCHAR(100) NOT NULL PRIMARY KEY,
    nilai VARCHAR(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
