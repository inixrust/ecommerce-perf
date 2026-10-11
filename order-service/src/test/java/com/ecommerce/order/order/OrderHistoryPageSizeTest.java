package com.ecommerce.order.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 3.1: riwayat order menolak ukuran halaman di atas
 * 100 dan nomor halaman negatif dengan 400 ProblemDetail.
 */
@IntegrationTest
@Tag("lat-3.1")
class OrderHistoryPageSizeTest {

    @Autowired
    MockMvc mvc;

    @Test
    void oversizedPageIsRejected() throws Exception {
        mvc.perform(get("/api/v1/customers/1/orders")
                        .param("size", "100000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void largestAllowedPageIsAccepted() throws Exception {
        mvc.perform(get("/api/v1/customers/1/orders").param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void negativePageIsRejected() throws Exception {
        mvc.perform(get("/api/v1/customers/1/orders").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
