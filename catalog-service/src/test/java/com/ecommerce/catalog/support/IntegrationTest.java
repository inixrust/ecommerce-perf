package com.ecommerce.catalog.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test integrasi dengan PostgreSQL asli (Testcontainers), MockMvc,
 * pencatat SQL, dan data uji kecil yang dimuat sekali per kelas.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfig.class, SqlRecorderConfig.class})
@Sql(scripts = "/test-data/catalog.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public @interface IntegrationTest {
}
