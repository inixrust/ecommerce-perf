package com.ecommerce.order.customer;

import java.math.BigDecimal;

/** Ringkasan belanja seorang pelanggan. */
public record CustomerSummary(
        Long customerId, long orderCount, BigDecimal totalSpent) {
}
