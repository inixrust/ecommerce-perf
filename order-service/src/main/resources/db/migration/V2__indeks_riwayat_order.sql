-- Latihan 2.2 (PERF-04): riwayat order memfilter customer_id lalu
-- mengurutkan created_at menurun. Indeks komposit ini melayani filter
-- dan urutan sekaligus, jadi LIMIT berhenti setelah 20 baris.
--
-- Di produksi, gunakan CREATE INDEX CONCURRENTLY agar tabel tidak
-- terkunci untuk penulisan selama indeks dibangun.
CREATE INDEX idx_orders_customer_created
    ON orders (customer_id, created_at DESC);
