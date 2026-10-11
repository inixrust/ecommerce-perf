#!/bin/bash
# Mengisi catalog_db dan order_db dengan data ber-volume.
#
# Tabel dibuat oleh Flyway saat service pertama kali start, jadi seed
# menunggu sampai tabelnya ada. Seed hanya berjalan pada database kosong.
# Data dibuat deterministik (setseed), sehingga sama di setiap laptop.
set -euo pipefail

run_psql() { # db user password [argumen psql lain...]
    local db=$1 user=$2 password=$3
    shift 3
    PGPASSWORD="$password" psql -h "$PGHOST" -U "$user" -d "$db" \
        -v ON_ERROR_STOP=1 -X -q "$@"
}

wait_for_table() { # db user password table
    for _ in $(seq 1 150); do
        if run_psql "$1" "$2" "$3" -tAc \
                "select to_regclass('public.$4') is not null" \
                | grep -q t; then
            return 0
        fi
        sleep 2
    done
    echo "Tabel $4 di $1 belum ada. Jalankan service-nya sekali" \
         "agar Flyway membuat skema, lalu ulangi seed." >&2
    exit 1
}

seed_database() { # db user password table expected sqlfile
    local db=$1 user=$2 password=$3 table=$4 expected=$5 sqlfile=$6
    wait_for_table "$db" "$user" "$password" "$table"
    local count
    count=$(run_psql "$db" "$user" "$password" -tAc \
        "select count(*) from $table")
    if [ "$count" -ge "$expected" ]; then
        echo "$db: sudah berisi $count baris $table, seed dilewati"
        return 0
    fi
    if [ "$count" -gt 0 ]; then
        echo "$db: berisi sebagian data ($count baris $table)." \
             "Reset dengan 'docker compose down -v' lalu ulangi." >&2
        exit 1
    fi
    local start=$SECONDS
    run_psql "$db" "$user" "$password" -f "$sqlfile"
    echo "$db: seed selesai dalam $((SECONDS - start)) detik"
}

total_start=$SECONDS

seed_database catalog_db "$CATALOG_DB_USER" "$CATALOG_DB_PASSWORD" \
    products 100000 /seed/catalog.sql
seed_database order_db "$ORDER_DB_USER" "$ORDER_DB_PASSWORD" \
    orders 50000 /seed/order.sql

echo "Total durasi seed: $((SECONDS - total_start)) detik"
run_psql catalog_db "$CATALOG_DB_USER" "$CATALOG_DB_PASSWORD" -c \
    "select current_database() as db,
            pg_size_pretty(pg_database_size(current_database())) as ukuran,
            (select count(*) from products) as products,
            (select count(*) from product_images) as images,
            (select count(*) from categories) as categories,
            (select round(avg(octet_length(description))) from products)
                as rata_rata_deskripsi_byte"
run_psql order_db "$ORDER_DB_USER" "$ORDER_DB_PASSWORD" -c \
    "select current_database() as db,
            pg_size_pretty(pg_database_size(current_database())) as ukuran,
            (select count(*) from customers) as customers,
            (select count(*) from orders) as orders,
            (select count(*) from order_items) as order_items"
