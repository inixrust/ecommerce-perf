package com.ecommerce.catalog.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.common.CacheConfig;
import com.ecommerce.catalog.support.IntegrationTest;
import com.ecommerce.catalog.support.SqlRecorder;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi final challenge: hasil pencarian tetap di-cache, tetapi
 * jumlah entry cache dibatasi walaupun kombinasi parameter tak terbatas.
 */
@IntegrationTest
@Tag("challenge")
class SearchResultCacheTest {

    // literal, bukan konstanta CacheConfig: test ini juga dijalankan
    // terhadap kode challenge yang belum memiliki cache tersebut
    private static final String SEARCH_RESULTS = "search-results";

    @Autowired
    MockMvc mvc;

    @Autowired
    SqlRecorder sql;

    @Autowired
    ProductSearchService search;

    @Autowired
    @Qualifier(CacheConfig.LOCAL)
    CacheManager localCaches;

    @Value("${catalog.search.cache.maximum-size:1000}")
    long maximumSize;

    @Test
    void repeatedSearchIsServedWithoutSql() throws Exception {
        var request = get("/api/v1/products/search")
                .param("q", "kaos")
                .param("after", "10")
                .param("size", "5");
        mvc.perform(request).andExpect(status().isOk());
        sql.reset();

        mvc.perform(request).andExpect(status().isOk());

        assertThat(sql.statements())
                .as("statement SQL pada pencarian kedua yang sama")
                .isEmpty();
    }

    @Test
    void cacheStaysWithinMaximumSize() {
        for (long after = 0; after < maximumSize + 100; after++) {
            search.search("kaos", after, 1);
        }

        Cache<?, ?> entries = (Cache<?, ?>) localCaches
                .getCache(SEARCH_RESULTS).getNativeCache();
        // eviction Caffeine berjalan asinkron; cleanUp menuntaskannya
        entries.cleanUp();

        assertThat(entries.estimatedSize())
                .as("entry cache setelah %d pencarian berbeda",
                        maximumSize + 100)
                .isLessThanOrEqualTo(maximumSize);
    }
}
