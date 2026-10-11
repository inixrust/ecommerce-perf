package com.ecommerce.order.client;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Alamat dan batas waktu layanan lain yang dipanggil order-service. */
@ConfigurationProperties("clients")
public record ClientProperties(Catalog catalog, Pricing pricing) {

    // TODO (Latihan 3.4)
    public record Catalog(URI baseUrl) {
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
