package com.ecommerce.order.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 3.3: harga semua item satu order diminta paralel,
 * dan pricing yang lambat tidak membuat request menggantung.
 */
@IntegrationTest
@Tag("lab-3.3")
class PricingFanOutTest {

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
    void pricesForOneOrderAreRequestedInParallel() throws Exception {
        for (long id = 501; id <= 505; id++) {
            stubs.product(id, "ACTIVE");
            // jeda cukup lama agar panggilan paralel pasti bertumpuk
            stubs.price(id, "1000.00", 300);
        }

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderFor(501, 502, 503, 504, 505)))
                .andExpect(status().isCreated());

        assertThat(stubs.maxConcurrentPricingRequests())
                .as("request harga yang dilayani bersamaan (5 item)")
                .isEqualTo(5);
    }

    @Test
    void slowPricingFailsFastWithGatewayTimeout() throws Exception {
        stubs.product(510, "ACTIVE");
        stubs.price(510, "1000.00", 3000);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderFor(510)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON));
    }

    private static String orderFor(long... productIds) {
        var items = new StringBuilder();
        for (long id : productIds) {
            if (!items.isEmpty()) {
                items.append(',');
            }
            items.append("{\"productId\": ").append(id)
                    .append(", \"quantity\": 1}");
        }
        return "{\"customerId\": 2, \"items\": [" + items + "]}";
    }
}
