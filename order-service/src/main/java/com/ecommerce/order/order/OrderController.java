package com.ecommerce.order.order;

import com.ecommerce.order.common.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orders;
    private final OrderHistoryService history;

    public OrderController(OrderService orders,
            OrderHistoryService history) {
        this.orders = orders;
        this.history = history;
    }

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderView> create(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderView order = orders.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + order.id()))
                .body(order);
    }

    // TODO (Latihan 3.1)
    @GetMapping("/api/v1/customers/{customerId}/orders")
    public PageResponse<OrderSummary> history(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return history.history(customerId, page, size);
    }
}
