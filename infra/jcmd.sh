#!/bin/bash
# Menjalankan jcmd terhadap JVM di container aplikasi.
#
# Image aplikasi berisi JRE tanpa jcmd. Skrip ini menjalankan jcmd dari
# container JDK yang berbagi PID namespace dengan container aplikasi,
# lalu menargetkan PID 1 (JVM aplikasi). Berkas yang diminta (rekaman
# JFR, thread dump) ditulis oleh JVM target, jadi gunakan path /out,
# yaitu folder infra/out di host.
#
# Pemakaian: infra/jcmd.sh <service> <perintah jcmd...>
# Contoh:
#   infra/jcmd.sh catalog-service Thread.print
#   infra/jcmd.sh catalog-service JFR.start duration=60s \
#       filename=/out/catalog.jfr settings=profile
#   infra/jcmd.sh order-service Thread.dump_to_file -format=json \
#       /out/order-threads.json
set -euo pipefail

if [ $# -lt 2 ]; then
    echo "Pemakaian: $0 <service> <perintah jcmd...>" >&2
    exit 2
fi
service=$1
shift

# Git Bash mengubah argumen berawalan / menjadi path Windows; matikan.
export MSYS_NO_PATHCONV=1
exec docker run --rm --pid="container:ecommerce-perf-${service}-1" \
    eclipse-temurin:21.0.12.1_1-jdk jcmd 1 "$@"
