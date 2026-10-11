package com.ecommerce.order.client;

/** Bagian data produk dari catalog-service yang dibutuhkan order. */
public record ProductInfo(
        Long id, String sku, String name, String status) {

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
