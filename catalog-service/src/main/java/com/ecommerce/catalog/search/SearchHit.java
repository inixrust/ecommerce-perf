package com.ecommerce.catalog.search;

import java.math.BigDecimal;

public record SearchHit(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        String snippet) {
}
