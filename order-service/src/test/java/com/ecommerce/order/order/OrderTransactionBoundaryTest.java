package com.ecommerce.order.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifikasi Latihan 3.2. Tepat saat stub catalog-service dan pricing
 * melayani request (aplikasi masih menunggu jawaban), test mencatat
 * berapa koneksi pool yang sedang dipinjam aplikasi. Harus 0.
 */
@IntegrationTest
@Tag("lab-3.2")
class OrderTransactionBoundaryTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    Stubs stubs;

    @Autowired
    DataSource dataSource;

    @Test
    void httpCallsDoNotHoldDatabaseConnection() throws Exception {
        stubs.reset();
        stubs.token(60, 0);
        for (long id = 401; id <= 403; id++) {
            stubs.product(id, "ACTIVE");
            stubs.price(id, "1000.00", 0);
        }
        var pool = dataSource.unwrap(HikariDataSource.class)
                .getHikariPoolMXBean();
        List<String> observations =
                Collections.synchronizedList(new ArrayList<>());
        stubs.onRequest(request -> observations.add(
                request + " koneksi=" + pool.getActiveConnections()));

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 5, "items": [
                                  {"productId": 401, "quantity": 1},
                                  {"productId": 402, "quantity": 1},
                                  {"productId": 403, "quantity": 1}]}
                                """))
                .andExpect(status().isCreated());

        // tiga panggilan catalog dan tiga panggilan harga; permintaan
        // token tidak dihitung karena token di-cache antar-test
        assertThat(observations)
                .as("koneksi pool yang dipinjam saat panggilan HTTP")
                .filteredOn(o -> !o.contains("/oauth/token"))
                .hasSize(6)
                .allMatch(o -> o.endsWith("koneksi=0"));
    }
}
