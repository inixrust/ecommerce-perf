package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 4.1: detail produk dilayani dari cache, dan cache
 * tidak menyajikan data lama setelah produk diubah.
 */
@IntegrationTest
@Tag("lab-4.1")
class ProductDetailCacheTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Autowired
    MeterRegistry meters;

    @Test
    void secondReadIsServedWithoutSql() throws Exception {
        mvc.perform(get("/api/v1/products/2")).andExpect(status().isOk());
        sql.reset();
        double borrowsBefore = connectionBorrows();

        mvc.perform(get("/api/v1/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("KAOS-0002"))
                .andExpect(jsonPath("$.categoryPath[2]").value("Kaos"));

        assertThat(sql.statements())
                .as("statement SQL pada pembacaan kedua detail produk")
                .isEmpty();
        assertThat(connectionBorrows() - borrowsBefore)
                .as("koneksi yang dipinjam pada pembacaan kedua")
                .isZero();
    }

    private double connectionBorrows() {
        return meters.get("hikaricp.connections.acquire").timer().count();
    }

    @Test
    void updateInvalidatesCachedDetail() throws Exception {
        mvc.perform(get("/api/v1/products/3")).andExpect(status().isOk());

        mvc.perform(put("/api/v1/products/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Kaos Edisi Baru", "price": 99000,
                                 "status": "ACTIVE"}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/products/3"))
                .andExpect(jsonPath("$.name").value("Kaos Edisi Baru"))
                .andExpect(jsonPath("$.price").value(99000));
    }
}
