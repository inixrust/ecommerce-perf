// GET /api/v1/categories/tree. Metrik penting selain latency:
// data_received (byte yang dikirim server ke klien).
//
// Setiap VU berperilaku seperti browser: menyimpan ETag terakhir dan
// mengirimnya kembali lewat If-None-Match. Server yang mendukung
// conditional GET menjawab 304 tanpa body.
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('CATALOG_URL', 'http://catalog-service:8081');

export const options = openModel('categories-tree');

// variabel modul: satu salinan per VU
let etag = null;

export default function () {
    const headers = etag ? { 'If-None-Match': etag } : {};
    const res = http.get(`${BASE}/api/v1/categories/tree`, {
        headers: headers,
        tags: { name: 'GET /api/v1/categories/tree' },
    });
    check(res, {
        'status 200 atau 304': (r) => r.status === 200 || r.status === 304,
    });
    if (res.headers['Etag']) {
        etag = res.headers['Etag'];
    }
}
