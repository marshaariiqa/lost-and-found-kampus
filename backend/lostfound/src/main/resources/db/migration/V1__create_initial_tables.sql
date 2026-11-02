
-- =====================================================
-- V1: INITIAL DATABASE - LOST AND FOUND KAMPUS
-- Database: MySQL
-- =====================================================

-- 1. ENUM TYPES akan dibuat sebagai ENUM MySQL langsung.

-- =====================================================
-- TABLE: PENGGUNA
-- =====================================================

CREATE TABLE pengguna (
    id_pengguna INT AUTO_INCREMENT PRIMARY KEY,
    nama_lengkap VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    kata_sandi VARCHAR(255) NULL,
    nomor_telepon VARCHAR(20),
    peran ENUM('MAHASISWA', 'ADMIN') NOT NULL DEFAULT 'MAHASISWA',
    provider ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL',
    google_id VARCHAR(255) NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: KATEGORI
-- =====================================================

CREATE TABLE kategori (
    id_kategori INT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: KEHILANGAN_BARANG
-- =====================================================

CREATE TABLE kehilangan_barang (
    id_kehilangan INT AUTO_INCREMENT PRIMARY KEY,
    id_pengguna INT NOT NULL,
    id_kategori INT NOT NULL,
    nama_barang VARCHAR(150) NOT NULL,
    deskripsi_publik TEXT,
    spesifikasi_rahasia TEXT,
    lokasi_kejadian VARCHAR(255),
    tanggal_kejadian DATETIME,
    url_gambar VARCHAR(255),
    status ENUM('TERSEDIA', 'SELESAI') NOT NULL DEFAULT 'TERSEDIA',

    CONSTRAINT fk_kehilangan_pengguna
        FOREIGN KEY (id_pengguna)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_kehilangan_kategori
        FOREIGN KEY (id_kategori)
        REFERENCES kategori(id_kategori)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_kehilangan_pengguna (id_pengguna),
    INDEX idx_kehilangan_kategori (id_kategori),
    INDEX idx_kehilangan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: PENEMU_BARANG
-- =====================================================

CREATE TABLE penemu_barang (
    id_penemu INT AUTO_INCREMENT PRIMARY KEY,
    id_pengguna INT NOT NULL,
    id_kategori INT NOT NULL,
    nama_barang VARCHAR(150) NOT NULL,
    deskripsi TEXT,
    lokasi_ditemukan VARCHAR(255),
    tanggal_ditemukan DATETIME,
    url_gambar VARCHAR(255),
    lokasi_penyimpanan VARCHAR(255),
    status ENUM('TERSEDIA', 'SELESAI') NOT NULL DEFAULT 'TERSEDIA',

    CONSTRAINT fk_penemu_pengguna
        FOREIGN KEY (id_pengguna)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_penemu_kategori
        FOREIGN KEY (id_kategori)
        REFERENCES kategori(id_kategori)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_penemu_pengguna (id_pengguna),
    INDEX idx_penemu_kategori (id_kategori),
    INDEX idx_penemu_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: KLAIM_ATAU_TANGGAPAN
-- =====================================================

CREATE TABLE klaim_atau_tanggapan (
    id_interaksi INT AUTO_INCREMENT PRIMARY KEY,
    id_kehilangan INT NULL,
    id_penemu INT NULL,
    id_merespons INT NOT NULL,
    id_admin_verifikator INT NULL,
    catatan_bukti TEXT,
    foto_ktm VARCHAR(255),
    alasan_penolakan TEXT,
    status_interaksi ENUM(
        'MENUNGGU_VERIFIKASI',
        'DISETUJUI',
        'SELESAI',
        'DITOLAK'
    ) NOT NULL DEFAULT 'MENUNGGU_VERIFIKASI',
    tenggat_tanggapan DATETIME,
    waktu_dibuat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    waktu_verifikasi DATETIME,

    CONSTRAINT fk_interaksi_kehilangan
        FOREIGN KEY (id_kehilangan)
        REFERENCES kehilangan_barang(id_kehilangan)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_interaksi_penemu
        FOREIGN KEY (id_penemu)
        REFERENCES penemu_barang(id_penemu)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_interaksi_responder
        FOREIGN KEY (id_merespons)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_interaksi_admin
        FOREIGN KEY (id_admin_verifikator)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_interaksi_laporan
        CHECK (id_kehilangan IS NOT NULL OR id_penemu IS NOT NULL),

    INDEX idx_interaksi_kehilangan (id_kehilangan),
    INDEX idx_interaksi_penemu (id_penemu),
    INDEX idx_interaksi_responder (id_merespons),
    INDEX idx_interaksi_admin (id_admin_verifikator),
    INDEX idx_interaksi_status (status_interaksi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: LOG_AKTIVITAS
-- =====================================================

CREATE TABLE log_aktivitas (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_kehilangan INT NULL,
    id_penemu INT NULL,
    id_admin INT NOT NULL,
    tindakan VARCHAR(255) NOT NULL,
    waktu_catat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_log_kehilangan
        FOREIGN KEY (id_kehilangan)
        REFERENCES kehilangan_barang(id_kehilangan)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_log_penemu
        FOREIGN KEY (id_penemu)
        REFERENCES penemu_barang(id_penemu)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_log_admin
        FOREIGN KEY (id_admin)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_log_barang
        CHECK (
            (id_kehilangan IS NOT NULL AND id_penemu IS NULL)
            OR
            (id_kehilangan IS NULL AND id_penemu IS NOT NULL)
        ),

    INDEX idx_log_kehilangan (id_kehilangan),
    INDEX idx_log_penemu (id_penemu),
    INDEX idx_log_admin (id_admin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- TABLE: NOTIFIKASI
-- =====================================================

CREATE TABLE notifikasi (
    id_notifikasi INT AUTO_INCREMENT PRIMARY KEY,
    id_pengguna INT NOT NULL,
    id_interaksi INT NULL,
    id_kehilangan INT NULL,
    id_penemu INT NULL,
    judul VARCHAR(150) NOT NULL,
    pesan TEXT NOT NULL,
    jenis_notifikasi ENUM(
        'INTERAKSI_BARU',
        'INTERAKSI_DISETUJUI',
        'INTERAKSI_DITOLAK',
        'BARANG_SELESAI',
        'PENGINGAT_TENGGAT'
    ) NOT NULL,
    status_dibaca BOOLEAN NOT NULL DEFAULT FALSE,
    waktu_dibuat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifikasi_pengguna
        FOREIGN KEY (id_pengguna)
        REFERENCES pengguna(id_pengguna)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_notifikasi_interaksi
        FOREIGN KEY (id_interaksi)
        REFERENCES klaim_atau_tanggapan(id_interaksi)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT fk_notifikasi_kehilangan
        FOREIGN KEY (id_kehilangan)
        REFERENCES kehilangan_barang(id_kehilangan)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT fk_notifikasi_penemu
        FOREIGN KEY (id_penemu)
        REFERENCES penemu_barang(id_penemu)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    INDEX idx_notifikasi_pengguna (id_pengguna),
    INDEX idx_notifikasi_dibaca (id_pengguna, status_dibaca),
    INDEX idx_notifikasi_waktu (waktu_dibuat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;