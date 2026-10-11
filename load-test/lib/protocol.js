// Protokol pengukuran yang dipakai semua skrip k6.
//
// Model beban TERBUKA (constant-arrival-rate): k6 memulai request baru
// dengan laju tetap, tidak peduli request sebelumnya sudah selesai atau
// belum. Model ini tidak terkena coordinated omission: saat server
// melambat, antrean terlihat di latency, bukan disembunyikan oleh k6
// yang ikut melambat.
//
// Dua skenario berurutan: "warmup" (tidak dinilai) lalu "measure".
// Threshold hanya membaca metrik bertag phase:measure.
//
// Variabel lingkungan (semua opsional):
//   RATE      request per detik (default dari slo.js)
//   WARMUP    durasi warmup, default 30s (protokol kelas)
//   DURATION  durasi pengukuran, default 60s
//   MAX_VUS   batas VU, default 400
import { SLO } from '../slo.js';

export function env(name, fallback) {
    const value = __ENV[name];
    return value === undefined || value === '' ? fallback : value;
}

export function openModel(sloName) {
    const slo = SLO[sloName];
    if (!slo) {
        throw new Error(`SLO "${sloName}" tidak ada di slo.js`);
    }
    const rate = Number(env('RATE', slo.rate));
    const warmup = env('WARMUP', '30s');
    const duration = env('DURATION', '60s');
    const maxVUs = Number(env('MAX_VUS', 400));
    const base = {
        executor: 'constant-arrival-rate',
        rate: rate,
        timeUnit: '1s',
        preAllocatedVUs: Math.min(100, maxVUs),
        maxVUs: maxVUs,
    };
    return {
        discardResponseBodies: true,
        scenarios: {
            warmup: Object.assign({}, base, {
                duration: warmup,
                tags: { phase: 'warmup' },
            }),
            measure: Object.assign({}, base, {
                duration: duration,
                startTime: warmup,
                tags: { phase: 'measure' },
            }),
        },
        thresholds: {
            'http_req_duration{phase:measure}': [
                `p(95)<${slo.p95}`,
                `p(99)<${slo.p99}`,
            ],
            'http_req_failed{phase:measure}': [`rate<${slo.errorRate}`],
            // run tidak sah bila k6 sendiri tidak sanggup menjaga laju
            // selama fase pengukuran
            'dropped_iterations{scenario:measure}': ['count==0'],
        },
        summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)',
            'p(99)', 'max'],
    };
}
