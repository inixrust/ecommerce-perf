// POST /api/v1/products/import: BATCH produk baru per request
// (default 1000). SKU dibuat unik per VU dan iterasi.
import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');
const BATCH = Number(env('BATCH', 1000));
const RUN_ID = Date.now().toString(36);

export const options = openModel('product-import');

export default function () {
    const prefix = `IMP-${RUN_ID}-${exec.scenario.iterationInTest}`;
    const products = [];
    for (let i = 0; i < BATCH; i++) {
        products.push({
            sku: `${prefix}-${i}`,
            name: `Produk Impor ${i}`,
            description: 'Produk hasil impor massal dari pemasok.',
            price: 10000 + i,
            status: 'DRAFT',
            categoryId: 111 + (i % 890),
        });
    }
    const res = http.post(`${BASE}/api/v1/products/import`,
        JSON.stringify({ products: products }), {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'POST /api/v1/products/import' },
            timeout: '60s',
        });
    check(res, { 'status 200': (r) => r.status === 200 });
}
