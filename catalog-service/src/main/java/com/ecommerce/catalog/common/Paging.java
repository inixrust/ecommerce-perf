package com.ecommerce.catalog.common;

/**
 * Batas ukuran halaman untuk semua endpoint daftar. Bukan sekadar
 * optimasi: tanpa batas, satu request size=100000 menghasilkan JSON
 * puluhan MB (OWASP API4:2023 Unrestricted Resource Consumption).
 */
public final class Paging {

    public static final int MAX_SIZE = 100;

    private Paging() {
    }
}
