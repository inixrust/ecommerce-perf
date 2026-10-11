// GET /api/v1/products/{id} dengan pola akses timpang: 80% request
// mengenai 1.000 produk "populer", sisanya tersebar ke 100.000 produk.
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');

export const options = openModel('product-detail');

function productId() {
    if (Math.random() < 0.8) {
        return 1 + Math.floor(Math.random() * 1000);
    }
    return 1 + Math.floor(Math.random() * 100000);
}

export default function () {
    const res = http.get(`${BASE}/api/v1/products/${productId()}`,
        { tags: { name: 'GET /api/v1/products/{id}' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
