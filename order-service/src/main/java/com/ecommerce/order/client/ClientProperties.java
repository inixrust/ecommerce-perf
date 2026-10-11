package com.ecommerce.order.client;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Alamat layanan lain yang dipanggil order-service. */
@ConfigurationProperties("clients")
public record ClientProperties(Catalog catalog, Pricing pricing) {

    public record Catalog(URI baseUrl) {
    }

    public record Pricing(
            URI baseUrl, String clientId, String clientSecret) {
    }
}
