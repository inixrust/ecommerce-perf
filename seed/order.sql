-- Seed order_db: 20.000 pelanggan, 50.000 order, 250.000 order item.
-- Sebaran sengaja timpang: 40% order milik 200 pelanggan pertama
-- (sekitar 100 order per pelanggan), seperti pelanggan B2B di dunia nyata.
SET max_parallel_workers_per_gather = 0;
SELECT setseed(0.42) \g /dev/null

BEGIN;

INSERT INTO customers (id, email, name)
SELECT g, 'customer' || g || '@example.com', 'Pelanggan ' || g
FROM generate_series(1, 20000) AS g;

SELECT setval(pg_get_serial_sequence('customers', 'id'), 20000)
\g /dev/null

INSERT INTO orders (id, customer_id, status, total, created_at)
SELECT g,
       CASE WHEN g % 5 < 2 THEN 1 + g % 200
            ELSE 201 + floor(random() * 19800)::int END,
       CASE WHEN g % 20 = 0 THEN 'CANCELLED'
            WHEN g % 10 = 1 THEN 'NEW'
            WHEN g % 3 = 0 THEN 'SHIPPED'
            ELSE 'PAID' END,
       0,
       TIMESTAMPTZ '2024-10-01 00:00:00+00'
           + random() * INTERVAL '730 days'
FROM generate_series(1, 50000) AS g;

SELECT setval(pg_get_serial_sequence('orders', 'id'), 50000) \g /dev/null

-- Lima item per order
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT 1 + (g - 1) / 5,
       1 + floor(random() * 100000)::int,
       1 + g % 3,
       (10000 + floor(random() * 4900) * 100)::numeric(12, 2)
FROM generate_series(1, 250000) AS g;

UPDATE orders o
SET total = s.total
FROM (SELECT order_id, sum(quantity * unit_price) AS total
      FROM order_items
      GROUP BY order_id) s
WHERE o.id = s.order_id;

COMMIT;

VACUUM ANALYZE customers;
VACUUM ANALYZE orders;
VACUUM ANALYZE order_items;
