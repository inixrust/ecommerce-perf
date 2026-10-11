package com.ecommerce.order.common;

/**
 * Batas ukuran halaman untuk semua endpoint daftar. Tanpa batas, satu
 * request dapat memaksa service memuat seluruh riwayat pelanggan
 * (OWASP API4:2023 Unrestricted Resource Consumption).
 */
public final class Paging {

    public static final int MAX_SIZE = 100;

    private Paging() {
    }
}
