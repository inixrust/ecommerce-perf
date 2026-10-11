package com.ecommerce.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.support.TestcontainersConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Demo PERF-12. Kompresi dikerjakan konektor Tomcat, jadi test ini
 * memakai server sungguhan (RANDOM_PORT) dan HttpClient JDK yang tidak
 * membuka gzip secara otomatis.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@Sql(scripts = "/test-data/catalog.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CompressionAndEtagTest {

    @LocalServerPort
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void largeJsonIsGzipped() throws Exception {
        // 40 produk sekitar 8 KB, di atas min-response-size (2 KB)
        var response = get("/api/v1/products?size=100", "gzip", null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Encoding"))
                .as("header Content-Encoding")
                .hasValue("gzip");
    }

    @Test
    void unchangedTreeIsNotModified() throws Exception {
        var first = get("/api/v1/categories/tree", null, null);
        String etag = first.headers().firstValue("ETag").orElse(null);
        assertThat(etag).as("header ETag pada respons pertama")
                .isNotNull();

        var second = get("/api/v1/categories/tree", null, etag);

        assertThat(second.statusCode())
                .as("status untuk If-None-Match: %s", etag)
                .isEqualTo(304);
        assertThat(second.body()).isEmpty();
    }

    private HttpResponse<byte[]> get(String path, String encoding,
            String ifNoneMatch) throws Exception {
        var request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + path));
        if (encoding != null) {
            request.header("Accept-Encoding", encoding);
        }
        if (ifNoneMatch != null) {
            request.header("If-None-Match", ifNoneMatch);
        }
        return http.send(request.build(),
                HttpResponse.BodyHandlers.ofByteArray());
    }
}
