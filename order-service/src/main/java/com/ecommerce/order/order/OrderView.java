package com.ecommerce.order.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Kontrak respons pembuatan order. Entity JPA tidak keluar service. */
public record OrderView(
        Long id,
        Long customerId,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        List<Line> items) {

    public record Line(
            Long productId, int quantity, BigDecimal unitPrice) {
    }

    static OrderView of(CustomerOrder order) {
        List<Line> lines = order.getItems().stream()
                .map(i -> new Line(i.getProductId(), i.getQuantity(),
                        i.getUnitPrice()))
                .toList();
        return new OrderView(order.getId(), order.getCustomer().getId(),
                order.getStatus(), order.getTotal(), order.getCreatedAt(),
                lines);
    }
}
