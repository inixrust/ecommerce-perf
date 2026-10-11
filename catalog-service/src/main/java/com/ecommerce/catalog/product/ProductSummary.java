package com.ecommerce.catalog.product;

import java.math.BigDecimal;

public record ProductSummary(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        ProductStatus status,
        String categoryName,
        String thumbnailUrl) {
}
