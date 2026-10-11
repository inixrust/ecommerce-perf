package com.ecommerce.catalog.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 2.5: pencarian memakai keyset pagination dengan
 * parameter after (id terakhir halaman sebelumnya) dan respons nextAfter.
 */
@IntegrationTest
@Tag("lab-2.5")
class KeysetSearchTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Test
    void onePageIsOneQueryWithoutCount() throws Exception {
        sql.reset();

        mvc.perform(get("/api/v1/products/search")
                        .param("q", "kaos")
                        .param("after", "10")
                        .param("size", "5"))
                .andExpect(status().isOk());

        assertThat(sql.statements())
                .as("statement SQL untuk satu halaman pencarian")
                .hasSize(1)
                .noneMatch(s -> s.toLowerCase(Locale.ROOT)
                        .contains("count("));
    }

    @Test
    void walkingWithAfterVisitsEveryMatchOnce() throws Exception {
        List<Long> seen = new ArrayList<>();
        Long after = null;
        for (int page = 0; page < 20; page++) {
            var request = get("/api/v1/products/search")
                    .param("q", "kaos")
                    .param("size", "7");
            if (after != null) {
                request.param("after", after.toString());
            }
            String body = mvc.perform(request)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(body)
                    .as("respons pencarian harus memuat field nextAfter")
                    .contains("\"nextAfter\"");
            List<Number> ids = JsonPath.read(body, "$.content[*].id");
            ids.forEach(id -> seen.add(id.longValue()));
            Number next = JsonPath.read(body, "$.nextAfter");
            if (next == null) {
                break;
            }
            after = next.longValue();
        }

        // data uji: 30 kaos dengan id 1-30
        assertThat(seen)
                .as("id yang dikunjungi saat berjalan dengan after")
                .containsExactlyElementsOf(
                        LongStream.rangeClosed(1, 30).boxed().toList());
    }
}
