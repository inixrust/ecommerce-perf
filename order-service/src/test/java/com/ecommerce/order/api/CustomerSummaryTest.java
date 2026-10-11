package com.ecommerce.order.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional ringkasan pelanggan. */
@IntegrationTest
class CustomerSummaryTest {

    @Autowired
    MockMvc mvc;

    @Test
    void summarizesOrdersOfCustomer() throws Exception {
        mvc.perform(get("/api/v1/customers/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(30))
                .andExpect(jsonPath("$.totalSpent").value(9000000));
    }

    @Test
    void customerWithoutOrdersHasZeroSummary() throws Exception {
        mvc.perform(get("/api/v1/customers/4/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(0));
    }
}
