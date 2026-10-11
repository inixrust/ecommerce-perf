package com.ecommerce.order.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.function.Consumer;

/** Stub HTTP yang dipakai berulang oleh test order-service. */
public class Stubs {

    private final WireMockServer catalog;
    private final WireMockServer pricing;
    private final HttpObserver catalogObserver;
    private final HttpObserver pricingObserver;

    Stubs(WireMockServer catalog, WireMockServer pricing,
            HttpObserver catalogObserver, HttpObserver pricingObserver) {
        this.catalog = catalog;
        this.pricing = pricing;
        this.catalogObserver = catalogObserver;
        this.pricingObserver = pricingObserver;
    }

    public void reset() {
        catalog.resetAll();
        pricing.resetAll();
        catalogObserver.reset();
        pricingObserver.reset();
    }

    /**
     * Callback yang dipanggil saat stub catalog atau pricing mulai
     * melayani request ("GET /api/v1/prices/401"), selagi aplikasi masih
     * menunggu jawaban.
     */
    public void onRequest(Consumer<String> callback) {
        catalogObserver.onRequest(callback);
        pricingObserver.onRequest(callback);
    }

    /** Jumlah maksimum request pricing yang dilayani bersamaan. */
    public int maxConcurrentPricingRequests() {
        return pricingObserver.maxInFlight();
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

    /** Header traceparent setiap request harga, null bila tidak ada. */
    public List<String> pricingTraceparents() {
        return pricing.findAll(
                getRequestedFor(urlPathMatching("/api/v1/prices/.*")))
                .stream()
                .map(request -> request.getHeader("traceparent"))
                .toList();
    }

    public int tokenRequests() {
        return pricing.findAll(
                postRequestedFor(urlPathMatching("/oauth/token")))
                .size();
    }
}
