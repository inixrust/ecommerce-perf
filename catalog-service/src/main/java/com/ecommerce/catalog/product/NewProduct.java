package com.ecommerce.catalog.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record NewProduct(
        @NotBlank @Size(max = 40) String sku,
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull ProductStatus status,
        @NotNull Long categoryId) {
}
