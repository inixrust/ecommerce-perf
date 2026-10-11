package com.ecommerce.order.client;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PricingClient {

    private final RestClient http;
    private final PricingTokenProvider tokens;

    public PricingClient(@Qualifier("pricingRestClient") RestClient http,
            PricingTokenProvider tokens) {
        this.http = http;
        this.tokens = tokens;
    }

    public BigDecimal price(Long productId) {
        PriceQuote quote = http.get()
                .uri("/api/v1/prices/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.token())
                .retrieve()
                .body(PriceQuote.class);
        return quote.price();
    }

    record PriceQuote(Long productId, BigDecimal price, String currency) {
    }
}
