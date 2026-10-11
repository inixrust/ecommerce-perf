package com.ecommerce.order.client;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.RestClient;

/**
 * RestClient.Builder dari Spring Boot sudah terpasang observability:
 * metrik http.client.requests dan propagasi trace.
 * EnableResilientMethods mengaktifkan @ConcurrencyLimit dan @Retryable.
 */
@Configuration(proxyBeanMethods = false)
@EnableResilientMethods
class RestClientConfig {

    @Bean
    RestClient catalogRestClient(RestClient.Builder builder,
            ClientProperties properties) {
        var catalog = properties.catalog();
        return builder
                .baseUrl(catalog.baseUrl())
                .requestFactory(requestFactory(catalog.connectTimeout(),
                        catalog.readTimeout()))
                .build();
    }

    @Bean
    RestClient pricingRestClient(RestClient.Builder builder,
            ClientProperties properties) {
        var pricing = properties.pricing();
        return builder
                .baseUrl(pricing.baseUrl())
                .requestFactory(requestFactory(pricing.connectTimeout(),
                        pricing.readTimeout()))
                .build();
    }

    private static JdkClientHttpRequestFactory requestFactory(
            Duration connectTimeout, Duration readTimeout) {
        // HTTP/1.1 eksplisit. Default HttpClient JDK adalah HTTP/2: untuk
        // http:// ia meng-upgrade ke h2c dan memultipleks semua request
        // ke satu koneksi, lalu gagal "too many concurrent streams" saat
        // ratusan request menunggu bersamaan.
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        // readTimeout di sini adalah tenggat total sejak request dikirim,
        // termasuk membaca body, bukan batas jeda antar-paket.
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }
}
