package com.ecommerce.order.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

/** Stub HTTP yang dipakai berulang oleh test order-service. */
public class Stubs {

    private final WireMockServer catalog;
    private final WireMockServer pricing;

    Stubs(WireMockServer catalog, WireMockServer pricing) {
        this.catalog = catalog;
        this.pricing = pricing;
    }

    public void reset() {
        catalog.resetAll();
        pricing.resetAll();
    }

    public void product(long id, String status) {
        catalog.stubFor(get("/api/v1/products/" + id)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id": %d, "sku": "SKU-%d",
                                 "name": "Produk %d", "status": "%s"}
                                """.formatted(id, id, id, status))));
    }

    public void unknownProduct(long id) {
        catalog.stubFor(get("/api/v1/products/" + id)
                .willReturn(aResponse().withStatus(404)));
    }

    public void token(int ttlSeconds, int delayMillis) {
        pricing.stubFor(post("/oauth/token")
                .willReturn(aResponse()
                        .withFixedDelay(delayMillis)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"access_token": "test-token",
                                 "expires_in": %d}
                                """.formatted(ttlSeconds))));
    }

    public void price(long productId, String price, int delayMillis) {
        pricing.stubFor(get("/api/v1/prices/" + productId)
                .willReturn(aResponse()
                        .withFixedDelay(delayMillis)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"productId": %d, "price": %s,
                                 "currency": "IDR"}
                                """.formatted(productId, price))));
    }

    public int priceRequests() {
        return pricing.findAll(
                getRequestedFor(urlPathMatching("/api/v1/prices/.*")))
                .size();
    }

    public int tokenRequests() {
        return pricing.findAll(
                postRequestedFor(urlPathMatching("/oauth/token")))
                .size();
    }
}
