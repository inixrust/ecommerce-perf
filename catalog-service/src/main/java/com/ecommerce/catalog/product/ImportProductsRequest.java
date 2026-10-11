package com.ecommerce.catalog.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ImportProductsRequest(
        @NotEmpty @Size(max = 1000) List<@Valid NewProduct> products) {
}
