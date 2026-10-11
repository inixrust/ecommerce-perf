// GET /api/v1/customers/{id}/orders untuk 200 pelanggan terbesar
// (sekitar 100 order per pelanggan pada data seed).
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('ORDER_URL', 'http://order-service:8082');

export const options = openModel('customer-orders');

export default function () {
    const customerId = 1 + Math.floor(Math.random() * 200);
    const res = http.get(
        `${BASE}/api/v1/customers/${customerId}/orders?page=0&size=20`,
        { tags: { name: 'GET /api/v1/customers/{id}/orders' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
