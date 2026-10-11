package com.ecommerce.catalog.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional endpoint baca; harus hijau di setiap tag. */
@IntegrationTest
class CatalogReadApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listFiltersByCategoryAndStatus() throws Exception {
        mvc.perform(get("/api/v1/products")
                        .param("categoryId", "3")
                        .param("status", "ACTIVE")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.content[0].sku").value("KAOS-0001"))
                .andExpect(jsonPath("$.content[0].categoryName")
                        .value("Kaos"))
                .andExpect(jsonPath("$.content[0].thumbnailUrl",
                        endsWith("KAOS-0001-1.jpg")));
    }

    @Test
    void detailReturnsProduct() throws Exception {
        mvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("KAOS-0001"))
                .andExpect(jsonPath("$.name").value("Kaos Polos Katun 1"));
    }

    @Test
    void unknownProductIsProblemDetail() throws Exception {
        mvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidPageSizeIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/products").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void categoryTreeIsNested() throws Exception {
        mvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Pakaian"))
                .andExpect(jsonPath("$[0].children[0].name")
                        .value("Atasan"))
                .andExpect(jsonPath("$[0].children[0].children",
                        hasSize(2)));
    }

    @Test
    void searchMatchesNameAndHighlightsSnippet() throws Exception {
        mvc.perform(get("/api/v1/products/search")
                        .param("q", "kaos KATUN!")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].sku").value("KAOS-0001"))
                .andExpect(jsonPath("$.content[0].snippet",
                        containsString("**Kaos**")));
    }

    @Test
    void searchWithoutKeywordIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/products/search").param("q", "!!"))
                .andExpect(status().isBadRequest());
    }
}
