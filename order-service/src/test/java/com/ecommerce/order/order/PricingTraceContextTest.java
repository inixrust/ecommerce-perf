package com.ecommerce.order.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 3.3: panggilan harga, juga yang berjalan paralel di
 * thread lain, tetap bagian dari trace request pembuatan order. Tanpa
 * propagasi konteks, setiap panggilan harga menjadi trace terpisah.
 */
@IntegrationTest
@Tag("lab-3.3")
class PricingTraceContextTest {

    private static final String TRACE_ID =
            "4bf92f3577b34da6a3ce929d0e0e4736";

    @Autowired
    MockMvc mvc;

    @Autowired
    Stubs stubs;

    @Test
    void pricingCallsBelongToTheOrderTrace() throws Exception {
        stubs.reset();
        stubs.token(60, 0);
        for (long id = 521; id <= 523; id++) {
            stubs.product(id, "ACTIVE");
            stubs.price(id, "1000.00", 0);
        }

        mvc.perform(post("/api/v1/orders")
                        .header("traceparent",
                                "00-" + TRACE_ID + "-00f067aa0ba902b7-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 2, "items": [
                                  {"productId": 521, "quantity": 1},
                                  {"productId": 522, "quantity": 1},
                                  {"productId": 523, "quantity": 1}]}
                                """))
                .andExpect(status().isCreated());

        assertThat(stubs.pricingTraceparents())
                .as("traceparent setiap panggilan harga")
                .hasSize(3)
                .allSatisfy(header -> assertThat(header).contains(TRACE_ID));
    }
}
