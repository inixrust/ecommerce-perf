// GET /api/v1/products/search: kata kunci acak, halaman acak sampai
// MAX_PAGE (default 100). Setiap kata kunci cocok dengan sekitar 1/30
// produk, jadi halaman 100 berarti melewati sekitar 2.000 hasil.
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');
const MAX_PAGE = Number(env('MAX_PAGE', 100));
const KEYWORDS = ['kaos', 'kemeja', 'celana', 'jaket', 'sepatu', 'tas',
    'topi', 'headphone', 'speaker', 'mouse', 'keyboard', 'lampu', 'kursi',
    'meja', 'botol', 'panci', 'gelas', 'payung'];

export const options = openModel('search');

export default function () {
    const q = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    const page = Math.floor(Math.random() * (MAX_PAGE + 1));
    const res = http.get(
        `${BASE}/api/v1/products/search?q=${q}&page=${page}&size=20`,
        { tags: { name: 'GET /api/v1/products/search' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
