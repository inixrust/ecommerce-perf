// GET /api/v1/categories/tree. Metrik penting selain latency:
// data_received (byte yang dikirim server ke klien).
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');

export const options = openModel('categories-tree');

export default function () {
    const res = http.get(`${BASE}/api/v1/categories/tree`,
        { tags: { name: 'GET /api/v1/categories/tree' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
