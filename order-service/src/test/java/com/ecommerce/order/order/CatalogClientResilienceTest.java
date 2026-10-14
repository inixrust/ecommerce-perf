package com.ecommerce.order.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 3.4: klien catalog punya batas waktu dan batas
 * jumlah panggilan bersamaan (bulkhead), sehingga catalog-service yang
 * lambat tidak menyeret seluruh order-service.
 */
@IntegrationTest
@Tag("lat-3.4")
class CatalogClientResilienceTest {

    private static final int CATALOG_LIMIT = 10;

    @Autowired
    MockMvc mvc;

    @Autowired
    Stubs stubs;

    @BeforeEach
    void setUp() {
        stubs.reset();
        stubs.token(60, 0);
    }

    @Test
    void slowCatalogFailsFastWithGatewayTimeout() throws Exception {
        stubs.product(601, "ACTIVE", 3000);
        stubs.price(601, "1000.00", 0);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(601)))
                .andExpect(status().isGatewayTimeout());
    }

    @Test
    void concurrentCatalogCallsAreBounded() throws Exception {
        for (long id = 611; id <= 640; id++) {
            stubs.product(id, "ACTIVE", 200);
            stubs.price(id, "1000.00", 0);
        }

        // 30 order bersamaan, masing-masing satu produk berbeda
        try (var executor = Executors.newFixedThreadPool(30)) {
            List<Future<Integer>> results = new ArrayList<>();
            for (long id = 611; id <= 640; id++) {
                String body = order(id);
                results.add(executor.submit(() -> mvc.perform(
                                post("/api/v1/orders")
                                        .contentType(
                                                MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andReturn().getResponse().getStatus()));
            }
            for (Future<Integer> result : results) {
                assertThat(result.get()).isEqualTo(201);
            }
        }

        assertThat(stubs.maxConcurrentCatalogRequests())
                .as("request catalog yang dilayani bersamaan (30 order)")
                .isLessThanOrEqualTo(CATALOG_LIMIT);
    }

    private static String order(long productId) {
        return """
                {"customerId": 3, "items": [
                  {"productId": %d, "quantity": 1}]}
                """.formatted(productId);
    }
}
