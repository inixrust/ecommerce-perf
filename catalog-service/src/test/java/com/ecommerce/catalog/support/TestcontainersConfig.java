package com.ecommerce.catalog.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** PostgreSQL dan Redis asli, versi sama dengan infra/compose.yaml. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    public static final String POSTGRES_IMAGE = "postgres:17.11";
    public static final String REDIS_IMAGE = "redis:7.4.11-alpine";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName("catalog_db");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>(REDIS_IMAGE).withExposedPorts(6379);
    }
}
