package com.ecommerce.catalog.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional produk populer. */
@IntegrationTest
class PopularProductsTest {

    @Autowired
    MockMvc mvc;

    @Test
    void mostViewedProductComesFirst() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(get("/api/v1/products/36"))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/api/v1/products/37")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/products/popular").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(36));
    }
}
