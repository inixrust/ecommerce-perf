package com.ecommerce.catalog.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 4.2: pohon kategori dilayani dari cache, dan
 * kategori baru langsung terlihat setelah dibuat.
 */
@IntegrationTest
@Tag("lat-4.2")
class CategoryTreeCacheTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void secondReadIsServedWithoutSql() throws Exception {
        mvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk());
        sql.reset();

        mvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pakaian"));

        assertThat(sql.statements())
                .as("statement SQL pada pembacaan kedua pohon kategori")
                .isEmpty();
    }

    @Test
    void newCategoryAppearsImmediately() throws Exception {
        mvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Sepeda", "parentId": null}"""))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/categories/tree"))
                .andExpect(jsonPath("$[*].name", hasItem("Sepeda")));
    }
}
