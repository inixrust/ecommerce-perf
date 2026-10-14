package com.ecommerce.order.client;

import com.ecommerce.order.common.OrderRejectedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient http;

    public CatalogClient(@Qualifier("catalogRestClient") RestClient http) {
        this.http = http;
    }

    // Bulkhead: panggilan di atas batas menunggu giliran, sehingga
    // catalog-service yang lambat tidak menyandera semua thread kita.
    @ConcurrencyLimit(
            limitString = "${clients.catalog.max-concurrent-calls}")
    public ProductInfo product(Long productId) {
        return http.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        (request, response) -> {
                            throw new OrderRejectedException(
                                    "Product " + productId
                                            + " does not exist");
                        })
                .body(ProductInfo.class);
    }
}
