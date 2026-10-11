# Kontrak API

Kontrak ini berlaku untuk tag `start`. Perubahan kontrak pada tag
berikutnya dicatat di bagian "Riwayat perubahan" di akhir berkas.

Respons JSON di atas 2 KB dikompresi gzip bila klien mengirim
`Accept-Encoding: gzip`.

Semua error memakai format ProblemDetail (RFC 9457) dengan
`Content-Type: application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Resource not found",
  "status": 404,
  "detail": "Product 999999 not found",
  "instance": "/api/v1/products/999999"
}
```

## catalog-service (port 8081)

### GET /api/v1/products

Daftar produk dengan filter opsional dan paginasi.

| Parameter    | Tipe    | Default | Keterangan                          |
| ------------ | ------- | ------- | ----------------------------------- |
| `categoryId` | long    | -       | filter kategori                     |
| `status`     | enum    | -       | `ACTIVE`, `INACTIVE`, atau `DRAFT`  |
| `page`       | int     | 0       | nomor halaman, mulai dari 0         |
| `size`       | int     | 20      | ukuran halaman, 1 sampai 100        |

Respons 200:

```json
{
  "content": [
    {
      "id": 89, "sku": "SKU-000089", "name": "Payung Klasik Katun 89",
      "price": 714791.00, "status": "ACTIVE",
      "categoryName": "Mainan Grup 10 Seri 90",
      "thumbnailUrl": "https://img.example.com/SKU-000089-1.jpg"
    }
  ],
  "page": 0, "size": 20, "totalElements": 90, "totalPages": 5
}
```

Error: 400 bila `size` di luar 1 sampai 100 atau `page` negatif.

### GET /api/v1/products/{id}

Detail produk beserta kategori, jalur kategori, dan gambar.
404 bila tidak ada.

```json
{
  "id": 5, "sku": "SKU-000005", "name": "Rok Polos Katun 5",
  "description": "...", "price": 49595.00, "status": "ACTIVE",
  "category": { "id": 116, "name": "Pakaian Grup 6 Seri 6" },
  "categoryPath": ["Pakaian", "Pakaian Grup 6", "Pakaian Grup 6 Seri 6"],
  "images": [ { "url": "https://img.example.com/SKU-000005-1.jpg",
                "position": 1 } ],
  "createdAt": "2025-01-01T00:25:00Z", "updatedAt": "2025-01-01T00:25:00Z"
}
```

### GET /api/v1/products/popular

Produk yang paling sering dilihat (detail produk) sejak service start.
Parameter `limit` 1 sampai 50 (default 10). Respons:
`[{ "productId": 36, "views": 5 }]`.

### PUT /api/v1/products/{id}

Mengubah nama, harga, dan status.

```json
{ "name": "Kaos Polos Katun", "price": 75000, "status": "ACTIVE" }
```

Respons 200 berisi produk terbaru. 400 bila validasi gagal, 404 bila
produk tidak ada.

### POST /api/v1/products/import

Impor massal, maksimal 1.000 produk per request.

```json
{
  "products": [
    { "sku": "IMP-1", "name": "Kaos Impor 1", "description": "...",
      "price": 10000, "status": "DRAFT", "categoryId": 111 }
  ]
}
```

Respons 200: `{ "imported": 1 }`. 400 bila validasi gagal atau ada
`categoryId` yang tidak dikenal.

### GET /api/v1/products/search

Pencarian berdasarkan nama produk dengan keyset pagination.

| Parameter | Tipe   | Default | Keterangan                                   |
| --------- | ------ | ------- | -------------------------------------------- |
| `q`       | string | wajib   | kata kunci; huruf dan angka, minimal 2 huruf |
| `after`   | long   | 0       | `nextAfter` dari respons sebelumnya          |
| `size`    | int    | 20      | jumlah hasil per potong, 1 sampai 100        |

Respons 200:

```json
{
  "content": [
    { "id": 150, "sku": "SKU-000150", "name": "Kaos Elegan Katun 150",
      "price": 207850.00, "snippet": "mudah rusak ..." }
  ],
  "nextAfter": 1050
}
```

`nextAfter` bernilai `null` pada potongan terakhir. Setiap hasil berisi
`snippet` (potongan deskripsi, kata yang cocok diapit `**`). 400 bila `q`
tidak berisi kata kunci atau `size` kurang dari 1.

### GET /api/v1/categories/tree

Seluruh pohon kategori:
`[{ "id": 1, "name": "Pakaian", "children": [ ... ] }]`.

Respons memuat header `ETag`. Request dengan `If-None-Match` berisi ETag
yang sama dijawab `304 Not Modified` tanpa body.

### POST /api/v1/categories

```json
{ "name": "Polo", "parentId": 2 }
```

Respons 201 dengan header `Location`. 404 bila `parentId` tidak ada.

## order-service (port 8082)

### POST /api/v1/orders

```json
{
  "customerId": 7,
  "items": [
    { "productId": 15, "quantity": 1 },
    { "productId": 23, "quantity": 2 }
  ]
}
```

Maksimal 20 baris, `quantity` 1 sampai 100. Untuk setiap baris,
order-service memanggil catalog-service (status produk) dan pricing
(harga). Respons 201 dengan header `Location`:

```json
{
  "id": 50001, "customerId": 7, "status": "NEW", "total": 5800.00,
  "createdAt": "2026-10-11T04:23:47.947Z",
  "items": [ { "productId": 15, "quantity": 1, "unitPrice": 1500.00 } ]
}
```

| Status | Kondisi                                         |
| ------ | ----------------------------------------------- |
| 400    | validasi request gagal                          |
| 404    | pelanggan tidak ada                             |
| 422    | produk tidak ada atau tidak berstatus `ACTIVE`  |
| 502    | layanan lain menjawab dengan respons yang tidak dapat dipakai |
| 503    | layanan lain tidak dapat dihubungi              |
| 504    | pricing tidak menjawab dalam batas waktu (1 detik) |

### GET /api/v1/customers/{id}/orders

Riwayat order pelanggan, terbaru lebih dulu. Parameter `page` (default 0)
dan `size` (default 20). Setiap order memuat `id`, `status`, `total`,
`createdAt`, dan `items` (`productId`, `quantity`, `unitPrice`).
404 bila pelanggan tidak ada.

### GET /api/v1/customers/{id}/summary

Ringkasan belanja pelanggan: `{ "customerId": 1, "orderCount": 30,
"totalSpent": 9000000.00 }`. 404 bila pelanggan tidak ada.

## pricing-stub (port 8083, WireMock)

| Endpoint                   | Keterangan                                       |
| -------------------------- | ------------------------------------------------ |
| `POST /oauth/token`        | client credentials; `expires_in` dari `TOKEN_TTL_SECONDS` |
| `GET /api/v1/prices/{id}`  | butuh `Authorization: Bearer ...`; harga = id x 100 |

Latensi kedua endpoint berdistribusi lognormal, diatur lewat environment
variable di `infra/compose.yaml`.

## Riwayat perubahan

| Tag              | Perubahan                                                       |
| ---------------- | --------------------------------------------------------------- |
| `lab-2.5-solusi` | pencarian: `page` diganti `after`; respons memuat `nextAfter`, tanpa `totalElements` dan `totalPages` |
| `demo-perf-06`   | detail produk dan respons pembuatan order menjadi DTO; tanpa `hibernateLazyInitializer` dan tanpa data pelanggan |
| `demo-perf-09`   | `size` pada daftar produk dan pencarian dibatasi 1 sampai 100; di luar itu 400 |
| `demo-perf-12`   | kompresi gzip; ETag dan `304 Not Modified` pada pohon kategori |
| `lab-3.3-solusi` | pembuatan order dapat menjawab 504 (pricing lambat), 503, atau 502 |
| `challenge`      | endpoint baru: produk populer dan ringkasan pelanggan |
