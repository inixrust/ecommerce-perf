package com.ecommerce.order.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional ringkasan pelanggan. */
@IntegrationTest
class CustomerSummaryTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @Test
    void summarizesOrdersOfCustomer() throws Exception {
        mvc.perform(get("/api/v1/customers/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(30))
                .andExpect(jsonPath("$.totalSpent").value(9000000));
    }

    @Test
    void customerWithoutOrdersHasZeroSummary() throws Exception {
        mvc.perform(get("/api/v1/customers/4/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(0));
    }

    // bertag challenge: di kode challenge jalur 404 membocorkan koneksi
    @Test
    @Tag("challenge")
    void unknownCustomerIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/customers/999999/summary"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifikasi final challenge: jalur 404 mengembalikan koneksi ke pool.
     * Lebih banyak permintaan daripada ukuran pool; bila ada yang bocor,
     * permintaan berikutnya menunggu connection-timeout lalu gagal.
     */
    @Test
    @Tag("challenge")
    void notFoundPathReturnsConnectionToPool() throws Exception {
        var hikari = dataSource.unwrap(HikariDataSource.class);
        for (int i = 0; i <= hikari.getMaximumPoolSize(); i++) {
            mvc.perform(get("/api/v1/customers/999999/summary"))
                    .andExpect(status().isNotFound());
        }

        assertThat(hikari.getHikariPoolMXBean().getActiveConnections())
                .as("koneksi yang masih dipinjam setelah semua respons")
                .isZero();
    }
}
