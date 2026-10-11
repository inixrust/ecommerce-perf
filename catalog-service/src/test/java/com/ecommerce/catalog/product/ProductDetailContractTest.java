package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Demo PERF-06: detail produk adalah DTO yang dimuat seluruhnya di
 * service, bukan entity yang dilengkapi lazy loading saat serialisasi.
 */
@IntegrationTest
class ProductDetailContractTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void detailIsDtoLoadedWithFixedQueries() throws Exception {
        sql.reset();

        mvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("KAOS-0001"))
                .andExpect(jsonPath("$.category.name").value("Kaos"))
                .andExpect(jsonPath("$.categoryPath",
                        contains("Pakaian", "Atasan", "Kaos")))
                .andExpect(jsonPath("$.images", hasSize(2)))
                .andExpect(jsonPath("$.images[0].url",
                        endsWith("KAOS-0001-1.jpg")))
                .andExpect(content().string(
                        not(containsString("hibernateLazyInitializer"))));

        // produk + kategori + gambar dalam satu query, jalur kategori satu
        assertThat(sql.count())
                .as("jumlah statement SQL untuk detail produk:%n%s",
                        String.join("\n", sql.statements()))
                .isLessThanOrEqualTo(2);
    }
}
