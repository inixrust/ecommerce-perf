# SLO per endpoint

Nilai di bawah ini juga tertulis di `load-test/slo.js` dan dipakai sebagai
threshold k6 pada fase measure. Laju adalah request per detik dengan model beban
terbuka (`constant-arrival-rate`).

| Endpoint | Skrip k6 | Laju | p95 | p99 | Error |
| -------- | -------- | ---- | --- | --- | ----- |
| `GET /api/v1/products` | `products-list` | 100 | 100 ms | 250 ms | < 1% |
| `GET /api/v1/products/{id}` | `product-detail` | 2000 | 50 ms | 150 ms | < 1% |
| `GET /api/v1/products/search` | `search` | 30 | 100 ms | 250 ms | < 1% |
| `GET /api/v1/categories/tree` | `categories-tree` | 1000 | 100 ms | 250 ms | < 1% |
| `POST /api/v1/orders` | `orders-create` | 40 | 500 ms | 1000 ms | < 1% |
| `GET /api/v1/customers/{id}/orders` | `customer-orders` | 100 | 100 ms | 250 ms | < 1% |
| `POST /api/v1/products/import` | `product-import` | 1 | 150 ms | 300 ms | < 1% |

## Cara membacanya

- SLO dikalibrasi di mesin pengukuran: aplikasi pada tag `start` melanggarnya, dan
  pada tag `final` memenuhinya dengan margin. Riwayat order adalah pengecualian:
  pada volume seed, endpoint itu sudah memenuhi SLO sejak `start`, dan perbaikannya
  terlihat di CPU PostgreSQL serta jumlah query.
- Laju detail produk dan pohon kategori sengaja tinggi. Pada laju rendah,
  pemborosan di endpoint ini hanya terlihat di CPU dan ukuran respons, belum di
  latency.
- Di laptop lain angka absolutnya berbeda. Bandingkan arah dan rasio sebelum dan
  sesudah perbaikan, dan atur laju dengan variabel `RATE` bila laptop lebih lambat.
- Sebuah run tidak sah bila k6 melaporkan `dropped_iterations` > 0 karena k6 sendiri
  kehabisan VU atau CPU. Bila service yang jenuh, catat sebagai overload dan laporkan
  error rate serta throughput yang tercapai, bukan hanya p99.
