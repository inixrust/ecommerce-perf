package com.ecommerce.order.order;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Demo PERF-06: respons pembuatan order adalah DTO. Entity yang
 * diserialisasi langsung ikut membawa data pelanggan (termasuk email).
 */
@IntegrationTest
class OrderResponseContractTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    Stubs stubs;

    @Test
    void responseIsDtoWithoutCustomerData() throws Exception {
        stubs.reset();
        stubs.token(60, 0);
        stubs.product(301, "ACTIVE");
        stubs.price(301, "1500.00", 0);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 4, "items": [
                                  {"productId": 301, "quantity": 2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(4))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.customer").doesNotExist())
                .andExpect(content().string(
                        not(containsString("@example.com"))));
    }
}
