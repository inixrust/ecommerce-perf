#!/bin/bash
# Dijalankan sekali oleh image postgres saat volume data masih kosong.
# Membuat satu role dan satu database per service, lalu memasang
# ekstensi pg_stat_statements di keduanya.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    <<-EOSQL
    CREATE ROLE ${CATALOG_DB_USER} LOGIN PASSWORD '${CATALOG_DB_PASSWORD}';
    CREATE DATABASE catalog_db OWNER ${CATALOG_DB_USER};
    CREATE ROLE ${ORDER_DB_USER} LOGIN PASSWORD '${ORDER_DB_PASSWORD}';
    CREATE DATABASE order_db OWNER ${ORDER_DB_USER};
EOSQL

for db in catalog_db order_db; do
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" \
        -c "CREATE EXTENSION IF NOT EXISTS pg_stat_statements;"
done
