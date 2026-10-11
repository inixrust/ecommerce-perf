// Beban campuran order-service: pembuatan order (memakai token pricing)
// berjalan bersamaan dengan riwayat order (hanya database). Dipakai
// untuk melihat dampak pinning pada request yang TIDAK memakai token.
//
// Variabel: CREATE_RATE (default 40), HISTORY_RATE (default 100),
// WARMUP, DURATION, MAX_VUS. Threshold memakai SLO di slo.js per
// endpoint, hanya pada fase measure.
import http from 'k6/http';
import { check } from 'k6';
import { env } from './lib/protocol.js';
import { SLO } from './slo.js';

const BASE = env('ORDER_URL', 'http://order-service:8082');
const WARMUP = env('WARMUP', '30s');
const DURATION = env('DURATION', '60s');
const MAX_VUS = Number(env('MAX_VUS', 400));

function scenario(fn, rate, phase, duration, startTime) {
    return {
        executor: 'constant-arrival-rate',
        exec: fn,
        rate: rate,
        timeUnit: '1s',
        duration: duration,
        startTime: startTime,
        preAllocatedVUs: 50,
        maxVUs: MAX_VUS,
        tags: { phase: phase },
    };
}

const createRate = Number(env('CREATE_RATE', 40));
const historyRate = Number(env('HISTORY_RATE', 100));
const create = SLO['orders-create'];
const history = SLO['customer-orders'];

export const options = {
    discardResponseBodies: true,
    scenarios: {
        createWarmup: scenario('createOrder', createRate, 'warmup',
            WARMUP, '0s'),
        historyWarmup: scenario('readHistory', historyRate, 'warmup',
            WARMUP, '0s'),
        createMeasure: scenario('createOrder', createRate, 'measure',
            DURATION, WARMUP),
        historyMeasure: scenario('readHistory', historyRate, 'measure',
            DURATION, WARMUP),
    },
    thresholds: {
        'http_req_duration{phase:measure,endpoint:create}': [
            `p(95)<${create.p95}`, `p(99)<${create.p99}`],
        'http_req_duration{phase:measure,endpoint:history}': [
            `p(95)<${history.p95}`, `p(99)<${history.p99}`],
        'http_req_failed{phase:measure}': ['rate<0.01'],
    },
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)',
        'p(99)', 'max'],
};

function activeProductId() {
    let id;
    do {
        id = 1 + Math.floor(Math.random() * 100000);
    } while (Math.floor(id / 7) % 10 < 2);
    return id;
}

export function createOrder() {
    const items = [];
    for (let i = 0; i < 5; i++) {
        items.push({ productId: activeProductId(), quantity: 1 });
    }
    const res = http.post(`${BASE}/api/v1/orders`, JSON.stringify({
        customerId: 1 + Math.floor(Math.random() * 20000),
        items: items,
    }), {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'POST /api/v1/orders', endpoint: 'create' },
    });
    check(res, { 'status 201': (r) => r.status === 201 });
}

export function readHistory() {
    const customerId = 1 + Math.floor(Math.random() * 200);
    const res = http.get(
        `${BASE}/api/v1/customers/${customerId}/orders?page=0&size=20`,
        { tags: { name: 'GET /api/v1/customers/{id}/orders',
            endpoint: 'history' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
