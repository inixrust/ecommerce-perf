-- Seed catalog_db: 1.000 kategori, 100.000 produk, 300.000 gambar.
-- Deterministik: setseed dan tanpa paralelisme.
SET max_parallel_workers_per_gather = 0;
SELECT setseed(0.42) \g /dev/null

BEGIN;

-- 10 kategori akar (id 1-10)
INSERT INTO categories (id, name, parent_id)
SELECT g, (ARRAY['Pakaian', 'Alas Kaki', 'Aksesoris', 'Elektronik',
                 'Komputer', 'Rumah Tangga', 'Dapur', 'Olahraga',
                 'Mainan', 'Kecantikan'])[g], NULL
FROM generate_series(1, 10) AS g;

-- 100 kategori tingkat 2 (id 11-110), 10 per akar
INSERT INTO categories (id, name, parent_id)
SELECT 10 + g, c.name || ' Grup ' || ((g - 1) % 10 + 1), c.id
FROM generate_series(1, 100) AS g
JOIN categories c ON c.id = (g - 1) / 10 + 1;

-- 890 kategori daun (id 111-1000), tempat semua produk berada
INSERT INTO categories (id, name, parent_id)
SELECT 110 + g, c.name || ' Seri ' || g, c.id
FROM generate_series(1, 890) AS g
JOIN categories c ON c.id = 11 + (g - 1) % 100;

SELECT setval(pg_get_serial_sequence('categories', 'id'), 1000)
\g /dev/null

-- 100.000 produk. Nama: benda + sifat + bahan, misalnya
-- "Kaos Polos Katun 123". Deskripsi 150-269 kata (sekitar 1-2 KB).
WITH v AS (
    SELECT ARRAY['kaos', 'kemeja', 'celana', 'jaket', 'sweater', 'rok',
                 'sepatu', 'sandal', 'tas', 'dompet', 'topi', 'kacamata',
                 'jam', 'headphone', 'speaker', 'mouse', 'keyboard',
                 'monitor', 'lampu', 'kursi', 'meja', 'rak', 'bantal',
                 'selimut', 'botol', 'panci', 'wajan', 'piring', 'gelas',
                 'payung'] AS nouns,
           ARRAY['polos', 'premium', 'klasik', 'sporti', 'ringan',
                 'elegan', 'minimalis', 'modern', 'vintage', 'kokoh',
                 'lembut', 'tebal', 'praktis', 'mewah', 'ekonomis',
                 'ringkas', 'santai', 'formal', 'unik', 'eksklusif']
               AS adjectives,
           ARRAY['katun', 'linen', 'denim', 'kulit', 'kanvas', 'wol',
                 'bambu', 'kayu', 'aluminium', 'plastik', 'keramik',
                 'kaca', 'baja', 'silikon', 'rotan'] AS materials,
           ARRAY['nyaman', 'dipakai', 'harian', 'tahan', 'lama', 'mudah',
                 'dibersihkan', 'cocok', 'untuk', 'rumah', 'kantor',
                 'perjalanan', 'dengan', 'desain', 'rapi', 'dan', 'warna',
                 'pilihan', 'bahan', 'berkualitas', 'tinggi', 'garansi',
                 'resmi', 'satu', 'tahun', 'ukuran', 'tersedia', 'lengkap',
                 'jahitan', 'kuat', 'tidak', 'mudah', 'rusak', 'ringan',
                 'dibawa', 'ke', 'mana', 'saja', 'produk', 'lokal',
                 'kualitas', 'ekspor', 'dikemas', 'aman', 'dikirim',
                 'cepat', 'stok', 'terbatas', 'favorit', 'pelanggan',
                 'hemat', 'energi', 'ramah', 'lingkungan', 'tampilan',
                 'menarik', 'fungsional', 'serbaguna', 'awet', 'presisi']
               AS words
)
INSERT INTO products (id, sku, name, description, price, status,
                      category_id, created_at, updated_at)
SELECT g,
       'SKU-' || lpad(g::text, 6, '0'),
       initcap(v.nouns[1 + g % 30]) || ' '
           || initcap(v.adjectives[1 + (g / 30) % 20]) || ' '
           || initcap(v.materials[1 + (g / 600) % 15]) || ' ' || g,
       (SELECT string_agg(t.word, ' ')
        FROM (SELECT v.words[1 + floor(random() * 60)::int] AS word
              FROM generate_series(1, 150 + g % 120)) AS t),
       10000 + (g::bigint * 7919) % 990000,
       -- status 80% ACTIVE, dan setiap kategori memuat ketiga status
       CASE (g / 7) % 10 WHEN 0 THEN 'INACTIVE' WHEN 1 THEN 'DRAFT'
                   ELSE 'ACTIVE' END,
       111 + g % 890,
       TIMESTAMPTZ '2025-01-01 00:00:00+00' + g * INTERVAL '5 minutes',
       TIMESTAMPTZ '2025-01-01 00:00:00+00' + g * INTERVAL '5 minutes'
FROM generate_series(1, 100000) AS g, v;

SELECT setval(pg_get_serial_sequence('products', 'id'), 100000)
\g /dev/null

-- Tiga gambar per produk
INSERT INTO product_images (product_id, url, position)
SELECT p.id, 'https://img.example.com/' || p.sku || '-' || pos || '.jpg',
       pos
FROM products p CROSS JOIN generate_series(1, 3) AS pos
ORDER BY p.id, pos;

-- Bila skema sudah memakai sequence Hibernate (demo batch insert),
-- selaraskan agar ID baru tidak bertabrakan dengan data seed. Pooled
-- optimizer memperlakukan nilai sequence sebagai batas atas blok 50 ID,
-- jadi nilai awalnya 100000 + 50.
DO $$
BEGIN
    IF to_regclass('public.products_seq') IS NOT NULL THEN
        PERFORM setval('products_seq', 100050, false);
    END IF;
END $$;

COMMIT;

VACUUM ANALYZE categories;
VACUUM ANALYZE products;
VACUUM ANALYZE product_images;
