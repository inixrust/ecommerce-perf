package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Demo PERF-08: daftar produk tidak membaca kolom yang tidak ditampilkan
 * (description, rata-rata 1-2 KB per baris pada data seed).
 */
@IntegrationTest
class ProductListProjectionTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void listSelectsOnlyDisplayedColumns() throws Exception {
        sql.reset();

        mvc.perform(get("/api/v1/products")
                        .param("categoryId", "3")
                        .param("status", "ACTIVE")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.content[0].categoryName")
                        .value("Kaos"))
                .andExpect(jsonPath("$.content[0].thumbnailUrl",
                        endsWith("KAOS-0001-1.jpg")));

        assertThat(sql.statements())
                .as("statement SQL untuk satu halaman daftar produk")
                .hasSizeLessThanOrEqualTo(2)
                .noneMatch(s -> s.contains("description"));
    }
}
