// GET /api/v1/products/search dengan keyset pagination (parameter
// after). Kedalaman halaman acak sampai MAX_PAGE (default 100), sama
// dengan versi offset sebelumnya agar hasilnya dapat dibandingkan.
//
// Klien sungguhan memakai nextAfter dari respons sebelumnya. Di sini
// kedalaman disimulasikan: setiap kata kunci cocok dengan sekitar 1/30
// produk (id tersebar merata), jadi halaman ke-n (20 hasil per halaman)
// dimulai kira-kira setelah id n x 20 x 30.
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
    const after = page * 20 * 30;
    const res = http.get(
        `${BASE}/api/v1/products/search?q=${q}&after=${after}&size=20`,
        { tags: { name: 'GET /api/v1/products/search' } });
    check(res, { 'status 200': (r) => r.status === 200 });
}
