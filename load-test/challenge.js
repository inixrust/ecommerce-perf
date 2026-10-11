// Beban final challenge: lalu lintas campuran seperti produksi.
// Variabel: WARMUP (default 30s), DURATION (default 120s), MAX_VUS
// (default 100 per skenario; antrean yang melebihinya tercatat sebagai
// dropped_iterations).
// SLO per endpoint dari slo.js, dinilai hanya pada fase measure.
import http from 'k6/http';
import { check } from 'k6';
import { env } from './lib/protocol.js';
import { SLO } from './slo.js';

const CATALOG = env('CATALOG_URL', 'http://catalog-service:8081');
const ORDER = env('ORDER_URL', 'http://order-service:8082');
const WARMUP = env('WARMUP', '30s');
const DURATION = env('DURATION', '120s');
const MAX_VUS = Number(env('MAX_VUS', 100));
const KEYWORDS = ['kaos', 'kemeja', 'celana', 'jaket', 'sepatu', 'tas',
    'topi', 'headphone', 'speaker', 'mouse', 'keyboard', 'lampu'];

const flows = {
    detail: { rate: 300, slo: SLO['product-detail'] },
    crawler: { rate: 150, slo: SLO['product-detail'] },
    popular: { rate: 20, slo: SLO['product-detail'] },
    search: { rate: 40, slo: SLO['search'] },
    summary: { rate: 60, slo: SLO['customer-orders'] },
    order: { rate: 20, slo: SLO['orders-create'] },
};

function scenario(flow, phase, duration, startTime) {
    return {
        executor: 'constant-arrival-rate',
        exec: flow,
        rate: flows[flow].rate,
        timeUnit: '1s',
        duration: duration,
        startTime: startTime,
        preAllocatedVUs: Math.max(10, Math.ceil(flows[flow].rate / 5)),
        maxVUs: MAX_VUS,
        gracefulStop: phase === 'warmup' ? '5s' : '30s',
        tags: { phase: phase, flow: flow },
    };
}

const scenarios = {};
const thresholds = {
    'http_req_failed{phase:measure}': ['rate<0.01'],
};
for (const flow of Object.keys(flows)) {
    scenarios[`${flow}Warmup`] = scenario(flow, 'warmup', WARMUP, '0s');
    scenarios[`${flow}Measure`] = scenario(flow, 'measure', DURATION,
        WARMUP);
    // dropped_iterations hanya bertag scenario, bukan tag kustom.
    // Toleransi: kurang dari kedatangan satu detik (jeda GC sesaat).
    thresholds[`dropped_iterations{scenario:${flow}Measure}`] = [
        `count<${flows[flow].rate}`];
    const slo = flows[flow].slo;
    thresholds[`http_req_duration{phase:measure,flow:${flow}}`] = [
        `p(95)<${slo.p95}`, `p(99)<${slo.p99}`];
}

export const options = {
    discardResponseBodies: true,
    scenarios: scenarios,
    thresholds: thresholds,
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)',
        'p(99)', 'max'],
};

function random(max) {
    return 1 + Math.floor(Math.random() * max);
}

export function detail() {
    // 60% lalu lintas ke 1.000 produk laris, sisanya menyebar ke katalog
    const id = Math.random() < 0.6 ? random(1000) : random(100000);
    const res = http.get(`${CATALOG}/api/v1/products/${id}`,
        { tags: { name: 'GET /api/v1/products/{id}' } });
    check(res, { 'detail 200': (r) => r.status === 200 });
}

export function crawler() {
    // bot mesin pencari membuka halaman produk mana pun di katalog
    const id = random(100000);
    const res = http.get(`${CATALOG}/api/v1/products/${id}`, {
        headers: { 'User-Agent': 'Mozilla/5.0 (compatible; Searchbot)' },
        tags: { name: 'GET /api/v1/products/{id}' },
    });
    check(res, { 'crawler 200': (r) => r.status === 200 });
}

export function popular() {
    const res = http.get(`${CATALOG}/api/v1/products/popular?limit=10`,
        { tags: { name: 'GET /api/v1/products/popular' } });
    check(res, { 'populer 200': (r) => r.status === 200 });
}

export function search() {
    const q = KEYWORDS[random(KEYWORDS.length) - 1];
    // klien melanjutkan dari kursor mana pun yang pernah mereka terima
    const after = random(100000) - 1;
    const res = http.get(
        `${CATALOG}/api/v1/products/search?q=${q}&after=${after}&size=20`,
        { tags: { name: 'GET /api/v1/products/search' } });
    check(res, { 'cari 200': (r) => r.status === 200 });
}

export function summary() {
    // sekitar 5% permintaan untuk id yang tidak ada (tautan lama)
    const id = random(21000);
    const res = http.get(`${ORDER}/api/v1/customers/${id}/summary`, {
        tags: { name: 'GET /api/v1/customers/{id}/summary' },
        responseCallback: http.expectedStatuses(200, 404),
    });
    check(res, { 'ringkasan 200/404': (r) => r.status < 500 });
}

export function order() {
    let id;
    do {
        id = random(100000);
    } while (Math.floor(id / 7) % 10 < 2);
    const res = http.post(`${ORDER}/api/v1/orders`, JSON.stringify({
        customerId: random(20000),
        items: [{ productId: id, quantity: 1 }],
    }), {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'POST /api/v1/orders' },
    });
    check(res, { 'order 201': (r) => r.status === 201 });
}
