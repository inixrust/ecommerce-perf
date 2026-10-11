#!/bin/bash
# Menjalankan satu skrip k6 di container (profil loadtest) dan menyimpan
# ringkasan JSON ke load-test/out/<label>.json.
#
# Pemakaian:
#   load-test/run.sh <skrip> [label]
# Contoh:
#   load-test/run.sh products-list sebelum
#   RATE=50 DURATION=30s load-test/run.sh search lab-2.5-starter
#
# Variabel yang diteruskan ke k6: RATE, WARMUP, DURATION, MAX_VUS,
# MAX_PAGE, ITEMS, BATCH, SIZE, CATALOG_URL, ORDER_URL.
# Exit code mengikuti k6: 0 = semua threshold lulus, 99 = ada yang gagal.
set -euo pipefail

if [ $# -lt 1 ]; then
    echo "Pemakaian: $0 <skrip-tanpa-.js> [label]" >&2
    exit 2
fi
script=$1
label=${2:-$script}

# Git Bash mengubah argumen berawalan / menjadi path Windows; matikan.
export MSYS_NO_PATHCONV=1

cd "$(dirname "$0")/../infra"
docker compose --profile loadtest run --rm --no-deps \
    -e RATE -e WARMUP -e DURATION -e MAX_VUS -e MAX_PAGE -e ITEMS \
    -e BATCH -e SIZE -e CATALOG_URL -e ORDER_URL \
    k6 run --quiet --summary-export="/out/${label}.json" \
    "/scripts/${script}.js"
