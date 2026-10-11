package com.ecommerce.catalog.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Demo PERF-09: ukuran halaman di atas batas ditolak dengan 400. */
@IntegrationTest
class PageSizeLimitTest {

    @Autowired
    MockMvc mvc;

    @Test
    void oversizedProductPageIsRejected() throws Exception {
        mvc.perform(get("/api/v1/products").param("size", "100000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void largestAllowedProductPageIsAccepted() throws Exception {
        mvc.perform(get("/api/v1/products").param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void oversizedSearchPageIsRejected() throws Exception {
        mvc.perform(get("/api/v1/products/search")
                        .param("q", "kaos")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativePageIsRejected() throws Exception {
        mvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
