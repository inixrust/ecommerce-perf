// GET /api/v1/products: daftar produk ACTIVE per kategori daun.
// SIZE (default 20) mengatur ukuran halaman yang diminta.
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');
const SIZE = Number(env('SIZE', 20));

export const options = openModel('products-list');

export default function () {
    // kategori daun pada data seed: id 111 sampai 1000
    const categoryId = 111 + Math.floor(Math.random() * 890);
    const res = http.get(
        `${BASE}/api/v1/products?categoryId=${categoryId}`
            + `&status=ACTIVE&page=0&size=${SIZE}`,
        { tags: { name: 'GET /api/v1/products' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
