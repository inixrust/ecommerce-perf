package com.ecommerce.order.client;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PricingClient {

    private final RestClient http;
    private final PricingTokenProvider tokens;
    // menyalin konteks thread pemanggil (trace, observation) ke setiap
    // tugas, agar panggilan harga tetap bagian dari trace request order
    private final ContextSnapshotFactory snapshots =
            ContextSnapshotFactory.builder().build();

    public PricingClient(@Qualifier("pricingRestClient") RestClient http,
            PricingTokenProvider tokens) {
        this.http = http;
        this.tokens = tokens;
    }

    /**
     * Harga semua produk satu order. Token diambil sekali di thread
     * pemanggil; panggilan harga berjalan paralel, satu virtual thread
     * per produk, sehingga waktu tunggunya kira-kira panggilan terlama,
     * bukan jumlah semuanya.
     */
    public Map<Long, BigDecimal> prices(Collection<Long> productIds) {
        String token = tokens.token();
        Map<Long, Future<BigDecimal>> pending = new LinkedHashMap<>();
        // close() menunggu semua tugas selesai sebelum keluar dari blok
        try (var executor = ContextExecutorService.wrap(
                Executors.newVirtualThreadPerTaskExecutor(), snapshots)) {
            for (Long id : productIds) {
                pending.put(id, executor.submit(() -> fetch(id, token)));
            }
            Map<Long, BigDecimal> result = new HashMap<>();
            for (var entry : pending.entrySet()) {
                result.put(entry.getKey(), join(entry.getValue()));
            }
            return result;
        }
    }

    private BigDecimal fetch(Long productId, String token) {
        PriceQuote quote = http.get()
                .uri("/api/v1/prices/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(PriceQuote.class);
        return quote.price();
    }

    private static BigDecimal join(Future<BigDecimal> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    record PriceQuote(Long productId, BigDecimal price, String currency) {
    }
}
