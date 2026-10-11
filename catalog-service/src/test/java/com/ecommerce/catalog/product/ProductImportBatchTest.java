package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Demo PERF-07: impor 120 produk dikirim sebagai beberapa batch JDBC,
 * bukan 120 eksekusi INSERT terpisah.
 */
@IntegrationTest
class ProductImportBatchTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void importUsesJdbcBatches() throws Exception {
        sql.reset();

        mvc.perform(post("/api/v1/products/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importBody(120)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(120));

        List<SqlRecorder.Execution> inserts = sql.executions().stream()
                .filter(e -> e.sql().toLowerCase(Locale.ROOT)
                        .startsWith("insert into products"))
                .toList();
        // 120 baris dengan batch_size 50: tiga eksekusi batch
        assertThat(inserts)
                .as("eksekusi JDBC untuk INSERT 120 produk")
                .hasSizeLessThanOrEqualTo(3)
                .allMatch(SqlRecorder.Execution::batch);
    }

    private static String importBody(int count) {
        var products = new StringJoiner(",", "{\"products\": [", "]}");
        for (int i = 0; i < count; i++) {
            products.add("""
                    {"sku": "BATCH-%d", "name": "Produk Batch %d",
                     "price": 1000, "status": "DRAFT", "categoryId": 3}
                    """.formatted(i, i));
        }
        return products.toString();
    }
}
