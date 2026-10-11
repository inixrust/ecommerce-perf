// POST /api/v1/orders: pelanggan acak, ITEMS produk (default 5).
// Produk dipilih dari id yang berstatus ACTIVE pada data seed
// (floor(id / 7) % 10 bernilai 2 sampai 9, lihat seed/catalog.sql).
import http from 'k6/http';
import { check } from 'k6';
import { env, openModel } from './lib/protocol.js';

const BASE = env('ORDER_URL', 'http://order-service:8082');
const ITEMS = Number(env('ITEMS', 5));

export const options = openModel('orders-create');

function activeProductId() {
    let id;
    do {
        id = 1 + Math.floor(Math.random() * 100000);
    } while (Math.floor(id / 7) % 10 < 2);
    return id;
}

export default function () {
    const items = [];
    for (let i = 0; i < ITEMS; i++) {
        items.push({
            productId: activeProductId(),
            quantity: 1 + (i % 3),
        });
    }
    const body = JSON.stringify({
        customerId: 1 + Math.floor(Math.random() * 20000),
        items: items,
    });
    const res = http.post(`${BASE}/api/v1/orders`, body, {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'POST /api/v1/orders' },
    });
    check(res, { 'status 201': (r) => r.status === 201 });
}
