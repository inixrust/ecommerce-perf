#!/bin/bash
# Verifikasi PERF-04 (Latihan 2.2): pada data seed, query daftar produk
# per kategori+status dan query riwayat order per pelanggan harus
# memakai indeks yang sesuai filternya.
#
# "Memakai indeks" saja tidak cukup: Index Scan pada primary key yang
# berjalan urut id lalu menyaring baris tetap membaca ribuan baris.
#
# Membutuhkan stack berjalan (postgres) dan data seed.
# Exit 0 = lulus, 1 = gagal.
set -uo pipefail
export MSYS_NO_PATHCONV=1
cd "$(dirname "$0")/../../infra"
set -a
. ./.env
set +a

failures=0

# check <db> <indeks-yang-diharapkan> <label> <query>
check() {
    local db=$1 expected=$2 label=$3 query=$4
    local plan
    plan=$(docker compose exec -T postgres psql -U "$POSTGRES_ADMIN_USER" \
        -d "$db" -AtX -c "EXPLAIN (COSTS OFF) $query") || {
        echo "GAGAL $label: query tidak dapat dijalankan"
        failures=$((failures + 1))
        return
    }
    local scan_re='(Seq|Index Only|Index|Bitmap Index) Scan'
    scan_re+='( Backward)?( using [a-z0-9_]+)?( on [a-z_]+)?'
    local scan
    scan=$(echo "$plan" | grep -oE "$scan_re" | head -1)
    if echo "$plan" | grep -qE "(using|on) $expected( |$)"; then
        echo "OK    $label: $scan"
    else
        echo "GAGAL $label: $scan (diharapkan memakai $expected)"
        failures=$((failures + 1))
    fi
}

# Bentuk query sama dengan SQL yang dihasilkan Hibernate
check catalog_db idx_products_category_status "daftar produk, halaman" \
    "select p.id from products p
     where p.category_id = 500 and p.status = 'ACTIVE'
     order by p.id offset 0 rows fetch first 20 rows only"
check catalog_db idx_products_category_status "daftar produk, count" \
    "select count(p.id) from products p
     where p.category_id = 500 and p.status = 'ACTIVE'"
check order_db idx_orders_customer_created "riwayat order, halaman" \
    "select o.id from orders o where o.customer_id = 17
     order by o.created_at desc offset 0 rows fetch first 20 rows only"
check order_db idx_orders_customer_created "riwayat order, count" \
    "select count(o.id) from orders o where o.customer_id = 17"

if [ "$failures" -gt 0 ]; then
    echo "PERF-04: $failures query belum memakai indeks yang sesuai"
    exit 1
fi
echo "PERF-04: semua query memakai indeks yang sesuai"
