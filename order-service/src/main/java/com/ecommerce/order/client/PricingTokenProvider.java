package com.ecommerce.order.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Token OAuth2 (client credentials) untuk pricing-service. Satu token
 * dipakai bersama semua request sampai hampir kedaluwarsa.
 */
@Component
public class PricingTokenProvider {

    private static final Duration REFRESH_MARGIN = Duration.ofSeconds(1);

    private final RestClient http;
    private final ClientProperties.Pricing properties;

    private String token;
    private Instant expiresAt = Instant.EPOCH;

    public PricingTokenProvider(
            @Qualifier("pricingRestClient") RestClient http,
            ClientProperties properties) {
        this.http = http;
        this.properties = properties.pricing();
    }

    public synchronized String token() {
        if (Instant.now().isAfter(expiresAt.minus(REFRESH_MARGIN))) {
            TokenResponse response = requestToken();
            token = response.accessToken();
            expiresAt = Instant.now().plusSeconds(response.expiresIn());
        }
        return token;
    }

    private TokenResponse requestToken() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        return http.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }

    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn) {
    }
}
