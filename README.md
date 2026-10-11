# ecommerce-perf

Aplikasi lab pelatihan **Spring Boot REST API Performance Tuning**
(INIXINDO): dua service Spring Boot 4 di atas Java 21, PostgreSQL 17,
Redis, stub layanan harga, observability dengan `grafana/otel-lgtm`, dan
load test k6.

Aplikasi ini sengaja **memuat masalah performa** yang ditemukan dan
diperbaiki selama pelatihan. Jangan dipakai sebagai contoh kode produksi
sebelum mencapai tag akhir.

## Isi repositori

| Folder             | Isi                                                        |
| ------------------ | ---------------------------------------------------------- |
| `catalog-service/` | produk, kategori, pencarian, impor (port 8081)             |
| `order-service/`   | pembuatan order dan riwayat order (port 8082)              |
| `infra/`           | `compose.yaml` dan semua berkas yang dirujuknya            |
| `seed/`            | pengisi data: 100.000 produk, 50.000 order                 |
| `load-test/`       | skrip k6, nilai SLO, pembungkus `run.sh`                   |
| `docs/`            | kontrak API (`api.md`) dan berkas `.http`                  |

## Prasyarat

- JDK 21 (disarankan Eclipse Temurin). `./mvnw -v` harus melaporkan
  Java 21; build menolak versi lain.
- Docker Desktop dengan alokasi RAM minimal 6 GB.
- Git Bash. Semua perintah di bawah memakai sintaks Bash.

## Menjalankan

```bash
# 1. Kredensial lokal (sekali saja), lalu ganti nilainya
cp infra/.env.example infra/.env

# 2. Build dan test (Testcontainers butuh Docker berjalan)
./mvnw verify

# 3. Semua service di container, termasuk aplikasi (profil bench).
#    Sebelum kelas (tanpa internet di kelas), pull semua image sekali:
#    docker compose -f infra/compose.yaml --profile bench \
#        --profile loadtest --profile tools --profile seed pull
docker compose -f infra/compose.yaml --profile bench up -d --wait

# 4. Isi data volume besar (sekitar 30-60 detik, hanya pada DB kosong)
docker compose -f infra/compose.yaml --profile seed run --rm seed

# 5. Load test singkat
RATE=50 DURATION=30s load-test/run.sh products-list
```

Setelah kode diubah: `./mvnw package -DskipTests` lalu
`docker compose -f infra/compose.yaml restart catalog-service`
(atau `order-service`). Jar di `target/` di-mount ke container.

### Menjalankan service dari IDE

Jalankan infrastruktur tanpa profil `bench`:

```bash
docker compose -f infra/compose.yaml up -d --wait
```

lalu jalankan `CatalogApplication` dan `OrderApplication` dari IDE.
Kredensial dibaca dari `infra/.env`. Seed dijalankan setelah kedua
service pernah start sekali (Flyway membuat tabelnya). k6 tetap berjalan
di container; arahkan ke host:

```bash
CATALOG_URL=http://host.docker.internal:8081 \
    load-test/run.sh products-list
```

## Diagnostik JVM di container

Image aplikasi berisi JRE tanpa `jcmd`. Gunakan `infra/jcmd.sh`, yang
menjalankan `jcmd` dari container JDK yang berbagi PID namespace:

```bash
infra/jcmd.sh catalog-service Thread.print
infra/jcmd.sh catalog-service JFR.start duration=60s \
    filename=/out/catalog.jfr settings=profile
```

Berkas di `/out` muncul di `infra/out/` pada host.

## Alamat penting

| Alamat                                   | Isi                                 |
| ---------------------------------------- | ----------------------------------- |
| http://localhost:3000                    | Grafana (metrik, trace, log)        |
| http://localhost:8081/actuator/prometheus | metrik catalog-service             |
| http://localhost:8082/actuator/prometheus | metrik order-service               |
| http://localhost:8083/__admin/mappings   | stub pricing (WireMock)             |

Semua port hanya dibuka di `127.0.0.1`.

## Mereset data

```bash
docker compose -f infra/compose.yaml --profile bench down -v
```

`-v` menghapus volume PostgreSQL, jadi seed perlu dijalankan ulang.
