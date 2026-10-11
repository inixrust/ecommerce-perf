-- Data uji kecil untuk test integrasi order-service.
TRUNCATE order_items, orders, customers RESTART IDENTITY CASCADE;

INSERT INTO customers (id, email, name)
SELECT g, 'pelanggan' || g || '@example.com', 'Pelanggan ' || g
FROM generate_series(1, 5) AS g;
SELECT setval(pg_get_serial_sequence('customers', 'id'), 5);

-- Pelanggan 1: 30 order lama, masing-masing 3 item, berselang 1 jam
INSERT INTO orders (customer_id, status, total, created_at)
SELECT 1, 'PAID', 300000,
       TIMESTAMPTZ '2026-01-01 00:00:00+00' + g * INTERVAL '1 hour'
FROM generate_series(1, 30) AS g;

-- Pelanggan 2: satu order
INSERT INTO orders (customer_id, status, total, created_at)
VALUES (2, 'NEW', 50000, TIMESTAMPTZ '2026-02-01 00:00:00+00');

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, 100 + i, 1, 100000
FROM orders o CROSS JOIN generate_series(1, 3) AS i
WHERE o.customer_id = 1;

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT id, 201, 1, 50000 FROM orders WHERE customer_id = 2;
