package com.ecommerce.order.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotEmpty @Size(max = 20) List<@Valid Line> items) {

    public record Line(
            @NotNull Long productId,
            @Positive @Max(100) int quantity) {
    }
}
