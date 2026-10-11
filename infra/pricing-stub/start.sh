#!/bin/sh
# Mengisi parameter latensi dari environment variable ke template
# mapping WireMock, lalu menjalankan WireMock.
set -eu

mkdir -p /home/wiremock/mappings
for template in /stub/templates/*.json; do
    sed -e "s/@PRICE_MEDIAN_MS@/${PRICE_MEDIAN_MS}/g" \
        -e "s/@PRICE_SIGMA@/${PRICE_SIGMA}/g" \
        -e "s/@TOKEN_MEDIAN_MS@/${TOKEN_MEDIAN_MS}/g" \
        -e "s/@TOKEN_SIGMA@/${TOKEN_SIGMA}/g" \
        -e "s/@TOKEN_TTL_SECONDS@/${TOKEN_TTL_SECONDS}/g" \
        "$template" > "/home/wiremock/mappings/$(basename "$template")"
done

# Tanpa request journal agar memori stub tidak tumbuh selama load test;
# respons async agar delay tidak menahan thread Jetty. Kapasitas thread
# dan antrean accept dibuat lega: stub tidak boleh menjadi bottleneck
# pengukuran, termasuk saat ratusan request menunggu bersamaan.
exec /docker-entrypoint.sh \
    --global-response-templating \
    --no-request-journal \
    --async-response-enabled=true \
    --async-response-threads=50 \
    --container-threads=500 \
    --jetty-accept-queue-size=2000 \
    --disable-banner
