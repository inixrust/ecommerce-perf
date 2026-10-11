package com.ecommerce.order.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** PostgreSQL asli, versi sama dengan infra/compose.yaml. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    public static final String POSTGRES_IMAGE = "postgres:17.11";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName("order_db");
    }
}
