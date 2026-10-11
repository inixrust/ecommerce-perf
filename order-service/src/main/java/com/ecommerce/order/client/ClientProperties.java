package com.ecommerce.order.client;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Alamat dan batas waktu layanan lain yang dipanggil order-service. */
@ConfigurationProperties("clients")
public record ClientProperties(Catalog catalog, Pricing pricing) {

    /** maxConcurrentCalls: batas panggilan bersamaan (bulkhead). */
    public record Catalog(URI baseUrl, Duration connectTimeout,
            Duration readTimeout, int maxConcurrentCalls) {
    }

    /**
     * connectTimeout: batas membuka koneksi TCP. readTimeout: batas
     * menunggu respons. Tanpa keduanya, request menunggu selamanya.
     */
    public record Pricing(
            URI baseUrl, String clientId, String clientSecret,
            Duration connectTimeout, Duration readTimeout) {
    }
}
