// SLO per endpoint: laju uji (request/detik), batas p95 dan p99 dalam
// milidetik, dan batas error rate. Nilai dikalibrasi di mesin pengukuran
// (lihat docs/slo.md). Di laptop lain, yang dibandingkan adalah arah dan
// rasio sebelum/sesudah, bukan angka absolutnya.
export const SLO = {
    'products-list':   { rate: 100,  p95: 100, p99: 250,  errorRate: 0.01 },
    'product-detail':  { rate: 2000, p95: 50,  p99: 150,  errorRate: 0.01 },
    'search':          { rate: 30,   p95: 100, p99: 250,  errorRate: 0.01 },
    'categories-tree': { rate: 1000, p95: 100, p99: 250,  errorRate: 0.01 },
    'orders-create':   { rate: 40,   p95: 500, p99: 1000, errorRate: 0.01 },
    'customer-orders': { rate: 100,  p95: 100, p99: 250,  errorRate: 0.01 },
    'product-import':  { rate: 1,    p95: 150, p99: 300,  errorRate: 0.01 },
};
