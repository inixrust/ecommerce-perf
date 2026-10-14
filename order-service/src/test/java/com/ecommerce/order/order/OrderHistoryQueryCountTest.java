package com.ecommerce.order.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.SqlRecorder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 2.4. Jumlah statement SQL untuk satu halaman
 * riwayat order tidak boleh bergantung pada jumlah order di halaman itu.
 */
@IntegrationTest
@Tag("lat-2.4")
class OrderHistoryQueryCountTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void onePageNeedsConstantNumberOfQueries() throws Exception {
        sql.reset();

        mvc.perform(get("/api/v1/customers/1/orders")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.content[19].items", hasSize(3)));

        // cek pelanggan, halaman, count, dan item untuk satu halaman
        assertThat(sql.count())
                .as("jumlah statement SQL untuk 20 order:%n%s",
                        String.join("\n", sql.statements()))
                .isLessThanOrEqualTo(4);
    }
}
