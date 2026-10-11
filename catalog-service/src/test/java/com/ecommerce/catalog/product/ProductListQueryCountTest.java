package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 2.3. Jumlah statement SQL untuk satu halaman daftar
 * produk tidak boleh bergantung pada jumlah produk di halaman itu.
 */
@IntegrationTest
@Tag("lab-2.3")
class ProductListQueryCountTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void onePageNeedsConstantNumberOfQueries() throws Exception {
        sql.reset();

        mvc.perform(get("/api/v1/products")
                        .param("status", "ACTIVE")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.content[19].categoryName")
                        .value("Kaos"))
                .andExpect(jsonPath("$.content[19].thumbnailUrl",
                        endsWith("KAOS-0020-1.jpg")));

        // halaman (dengan kategori), count, dan gambar untuk satu halaman
        assertThat(sql.count())
                .as("jumlah statement SQL untuk 20 produk:%n%s",
                        String.join("\n", sql.statements()))
                .isLessThanOrEqualTo(3);
    }
}
