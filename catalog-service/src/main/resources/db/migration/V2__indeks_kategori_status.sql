-- Latihan 2.2 (PERF-04): filter daftar produk adalah
-- category_id = ? AND status = ?. Keduanya equality; category_id di
-- depan agar indeks juga melayani query yang hanya memfilter kategori.
--
-- Di produksi, gunakan CREATE INDEX CONCURRENTLY agar tabel tidak
-- terkunci untuk penulisan selama indeks dibangun.
CREATE INDEX idx_products_category_status
    ON products (category_id, status);
