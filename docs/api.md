# Kontrak API

Kontrak ini berlaku untuk tag `start`. Perubahan kontrak pada tag
berikutnya dicatat di bagian "Riwayat perubahan" di akhir berkas.

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
| `size`       | int     | 20      | ukuran halaman                      |

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

Error: 400 bila `size` kurang dari 1 atau `page` negatif.

### GET /api/v1/products/{id}

Detail produk beserta gambar dan kategori. 404 bila tidak ada.

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

Pencarian berdasarkan nama produk.

| Parameter | Tipe   | Default | Keterangan                                  |
| --------- | ------ | ------- | ------------------------------------------- |
| `q`       | string | wajib   | kata kunci; huruf dan angka, minimal 2 huruf |
| `page`    | int    | 0       | nomor halaman                               |
| `size`    | int    | 20      | ukuran halaman                              |

Respons 200 berbentuk halaman yang sama dengan daftar produk; setiap hasil
berisi `id`, `sku`, `name`, `price`, dan `snippet` (potongan deskripsi,
kata yang cocok diapit `**`). 400 bila `q` tidak berisi kata kunci.

### GET /api/v1/categories/tree

Seluruh pohon kategori:
`[{ "id": 1, "name": "Pakaian", "children": [ ... ] }]`.

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
(harga). Respons 201 dengan header `Location`.

| Status | Kondisi                                         |
| ------ | ----------------------------------------------- |
| 400    | validasi request gagal                          |
| 404    | pelanggan tidak ada                             |
| 422    | produk tidak ada atau tidak berstatus `ACTIVE`  |

### GET /api/v1/customers/{id}/orders

Riwayat order pelanggan, terbaru lebih dulu. Parameter `page` (default 0)
dan `size` (default 20). Setiap order memuat `id`, `status`, `total`,
`createdAt`, dan `items` (`productId`, `quantity`, `unitPrice`).
404 bila pelanggan tidak ada.

## pricing-stub (port 8083, WireMock)

| Endpoint                   | Keterangan                                       |
| -------------------------- | ------------------------------------------------ |
| `POST /oauth/token`        | client credentials; `expires_in` dari `TOKEN_TTL_SECONDS` |
| `GET /api/v1/prices/{id}`  | butuh `Authorization: Bearer ...`; harga = id x 100 |

Latensi kedua endpoint berdistribusi lognormal, diatur lewat environment
variable di `infra/compose.yaml`.

## Riwayat perubahan

Belum ada perubahan dari tag `start`.
