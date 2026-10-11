package com.ecommerce.order.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient.Builder dari Spring Boot sudah terpasang observability:
 * metrik http.client.requests dan propagasi trace.
 */
@Configuration(proxyBeanMethods = false)
class RestClientConfig {

    @Bean
    RestClient catalogRestClient(RestClient.Builder builder,
            ClientProperties properties) {
        return builder
                .baseUrl(properties.catalog().baseUrl())
                .build();
    }

    @Bean
    RestClient pricingRestClient(RestClient.Builder builder,
            ClientProperties properties) {
        return builder
                .baseUrl(properties.pricing().baseUrl())
                .build();
    }
}
