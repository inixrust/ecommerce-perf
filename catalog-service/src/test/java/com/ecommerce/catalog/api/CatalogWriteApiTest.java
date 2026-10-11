package com.ecommerce.catalog.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional endpoint tulis; harus hijau di setiap tag. */
@IntegrationTest
class CatalogWriteApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void importCreatesProducts() throws Exception {
        String body = """
                {"products": [
                  {"sku": "IMP-1", "name": "Kaos Impor 1",
                   "price": 10000, "status": "ACTIVE", "categoryId": 3},
                  {"sku": "IMP-2", "name": "Kaos Impor 2",
                   "price": 12000, "status": "DRAFT", "categoryId": 3}
                ]}
                """;
        mvc.perform(post("/api/v1/products/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(2));
    }

    @Test
    void importRejectsUnknownCategory() throws Exception {
        String body = """
                {"products": [{"sku": "IMP-X", "name": "X", "price": 1,
                  "status": "ACTIVE", "categoryId": 999}]}
                """;
        mvc.perform(post("/api/v1/products/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesProduct() throws Exception {
        String body = """
                {"name": "Headphone Baru", "price": 450000,
                 "status": "INACTIVE"}
                """;
        mvc.perform(put("/api/v1/products/31")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/products/31"))
                .andExpect(jsonPath("$.name").value("Headphone Baru"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void createCategoryReturnsLocation() throws Exception {
        mvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Polo\", \"parentId\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Polo"));
    }
}
