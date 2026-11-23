-- Status akun pengguna (aktif / nonaktif) untuk manajemen pengguna admin
ALTER TABLE pengguna
ADD COLUMN aktif BOOLEAN NOT NULL DEFAULT TRUE;
