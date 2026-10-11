-- Data uji kecil untuk test integrasi catalog-service.
TRUNCATE product_images, products, categories RESTART IDENTITY CASCADE;

INSERT INTO categories (id, name, parent_id) VALUES
    (1, 'Pakaian', NULL),
    (2, 'Atasan', 1),
    (3, 'Kaos', 2),
    (4, 'Kemeja', 2),
    (5, 'Elektronik', NULL),
    (6, 'Audio', 5);
SELECT setval(pg_get_serial_sequence('categories', 'id'), 6);

-- 30 kaos (25 ACTIVE, 5 DRAFT) dan 10 headphone ACTIVE
INSERT INTO products (sku, name, description, price, status, category_id)
SELECT 'KAOS-' || lpad(g::text, 4, '0'),
       'Kaos Polos Katun ' || g,
       'Kaos nyaman berbahan katun combed untuk dipakai harian. '
           || 'Tersedia dalam banyak warna, jahitan rapi, dan tidak '
           || 'mudah melar setelah dicuci berkali-kali.',
       50000 + g * 1000,
       CASE WHEN g <= 25 THEN 'ACTIVE' ELSE 'DRAFT' END,
       3
FROM generate_series(1, 30) AS g;

INSERT INTO products (sku, name, description, price, status, category_id)
SELECT 'HDP-' || lpad(g::text, 4, '0'),
       'Headphone Nirkabel ' || g,
       'Headphone bluetooth dengan baterai tahan lama dan suara jernih.',
       300000 + g * 5000,
       'ACTIVE',
       6
FROM generate_series(1, 10) AS g;

-- Bila ID produk berasal dari sequence Hibernate (demo PERF-07),
-- naikkan sequence melewati data uji (lihat V3__sequence_produk.sql).
-- Sequence hanya boleh naik: aplikasi di konteks test yang dipakai
-- bersama masih memegang blok ID dari kelas test sebelumnya. is_called
-- selalu true agar pg_sequence_last_value tidak NULL di kelas berikutnya.
-- to_regclass bernilai NULL bila sequence belum ada; setval(NULL, ...)
-- tidak melakukan apa pun.
SELECT setval(to_regclass('public.products_seq'),
              greatest((SELECT max(id) FROM products),
                       coalesce(pg_sequence_last_value(
                           to_regclass('public.products_seq')), 0)) + 50,
              true);

-- Dua gambar per produk
INSERT INTO product_images (product_id, url, position)
SELECT p.id, 'https://img.example.com/' || p.sku || '-' || pos || '.jpg',
       pos
FROM products p CROSS JOIN generate_series(1, 2) AS pos;
