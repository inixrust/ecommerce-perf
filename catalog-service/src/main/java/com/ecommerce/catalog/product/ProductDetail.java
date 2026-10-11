package com.ecommerce.catalog.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Kontrak detail produk. Entity JPA tidak pernah keluar dari service. */
public record ProductDetail(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        CategoryRef category,
        List<String> categoryPath,
        List<Image> images,
        Instant createdAt,
        Instant updatedAt) {

    public record CategoryRef(Long id, String name) {
    }

    public record Image(String url, int position) {
    }
}
