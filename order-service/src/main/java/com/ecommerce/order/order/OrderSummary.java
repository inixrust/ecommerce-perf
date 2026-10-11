package com.ecommerce.order.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Satu baris riwayat order pelanggan. */
public record OrderSummary(
        Long id,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        List<Line> items) {

    public record Line(
            Long productId, int quantity, BigDecimal unitPrice) {
    }
}
