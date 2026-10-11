#!/bin/bash
# Verifikasi PERF-15 (Latihan 5.1): ukuran JVM catalog-service sesuai
# rencana kapasitas container 768 MB.
#
# Pemeriksaan:
#   1. limit memori container sesuai rencana (768 MB)
#   2. heap maksimum + memori non-heap terukur (NMT) <= 90% limit,
#      jadi heap boleh tumbuh penuh tanpa container dimatikan
#   3. log GC ditulis ke infra/out/gc-catalog.log
#   4. di bawah beban 60 detik container tidak mati (exit 137)
#   5. puncak memori container (cgroup memory.peak) <= 90% limit;
#      angka ini juga memuat malloc native yang tidak dicatat NMT
#
# Membutuhkan stack profil bench berjalan dan data seed. Jalankan pada
# container yang baru dibuat: memory.peak dihitung sejak container lahir.
# Exit 0 = lulus, 1 = gagal.
set -uo pipefail
export MSYS_NO_PATHCONV=1
here=$(cd "$(dirname "$0")" && pwd)
cd "$here/../../infra"

PLANNED_LIMIT_MB=768
container=ecommerce-perf-catalog-service-1
failures=0
ok() { echo "OK    $*"; }
fail() { echo "GAGAL $*"; failures=$((failures + 1)); }

# 1. limit memori container
limit=$(docker inspect "$container" --format '{{.HostConfig.Memory}}')
limit_mb=$((limit / 1024 / 1024))
if [ "$limit_mb" -eq "$PLANNED_LIMIT_MB" ]; then
    ok "limit memori ${limit_mb} MB"
else
    fail "limit memori ${limit_mb} MB (rencana ${PLANNED_LIMIT_MB} MB)"
fi

# 4. beban 60 detik: daftar produk 100 per halaman dan pencarian
before=$(docker inspect "$container" --format '{{.RestartCount}}')
RATE=150 SIZE=100 WARMUP=10s DURATION=60s \
    ../load-test/run.sh products-list perf-15-daftar > /dev/null 2>&1 &
RATE=30 WARMUP=10s DURATION=60s \
    ../load-test/run.sh search perf-15-cari > /dev/null 2>&1 &
wait
state=$(docker inspect "$container" \
    --format '{{.State.Status}} {{.State.OOMKilled}} {{.RestartCount}}')
read -r status oom restarts <<< "$state"
if [ "$status" = running ] && [ "$oom" = false ] \
        && [ "$restarts" = "$before" ]; then
    ok "tidak ada exit 137 selama beban 60 detik"
else
    fail "container status=$status OOMKilled=$oom (exit 137)"
fi

# 2. heap maksimum + non-heap dari Native Memory Tracking
if [ "$status" = running ]; then
    nmt=$(./jcmd.sh catalog-service VM.native_memory summary 2>&1)
    if ! echo "$nmt" | grep -q 'Total:'; then
        fail "NMT tidak aktif (tambahkan -XX:NativeMemoryTracking=summary)"
    else
        kb() { echo "$nmt" | grep "$1" | head -1 \
            | grep -oE "$2=[0-9]+KB" | grep -oE '[0-9]+'; }
        total=$(kb 'Total:' committed)
        heap_committed=$(kb 'Java Heap' committed)
        heap_max=$(kb 'Java Heap' reserved)
        nonheap=$((total - heap_committed))
        worst=$(( (heap_max + nonheap) / 1024 ))
        budget=$(( limit_mb * 90 / 100 ))
        line="heap maks $((heap_max / 1024)) MB + non-heap"
        line+=" $((nonheap / 1024)) MB = ${worst} MB"
        if [ "$worst" -le "$budget" ]; then
            ok "$line (batas ${budget} MB)"
        else
            fail "$line (batas ${budget} MB)"
        fi
    fi
fi

# 5. puncak memori container (cgroup), termasuk malloc di luar NMT
if [ "$status" = running ]; then
    peak=$(docker exec "$container" cat /sys/fs/cgroup/memory.peak)
    peak_mb=$((peak / 1024 / 1024))
    budget=$(( limit_mb * 90 / 100 ))
    if [ "$peak_mb" -le "$budget" ]; then
        ok "puncak memori container ${peak_mb} MB (batas ${budget} MB)"
    else
        fail "puncak memori container ${peak_mb} MB (batas ${budget} MB)"
    fi
fi

# 3. log GC
gc_log=out/gc-catalog.log
if [ -s "$gc_log" ] && grep -q 'Pause' "$gc_log"; then
    ok "log GC: $(grep -c 'Pause' "$gc_log") pause tercatat di $gc_log"
else
    fail "log GC tidak ada di infra/$gc_log"
fi

if [ "$failures" -gt 0 ]; then
    echo "PERF-15: $failures pemeriksaan gagal"
    exit 1
fi
echo "PERF-15: ukuran JVM sesuai rencana"
