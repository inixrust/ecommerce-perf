package com.ecommerce.catalog.product;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Menghitung berapa kali detail produk dilihat sejak service start, untuk
 * halaman "produk populer". Peringkat teratas disiapkan setiap kali ada
 * view, sehingga endpoint populer cukup mengembalikan daftar yang siap.
 */
@Component
public class ViewCounter {

    static final int TOP_SIZE = 50;

    private final Map<Long, Long> views = new HashMap<>();
    private List<PopularProduct> top = List.of();

    public synchronized void record(Long productId) {
        views.merge(productId, 1L, Long::sum);
        top = views.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_SIZE)
                .map(e -> new PopularProduct(e.getKey(), e.getValue()))
                .toList();
    }

    public synchronized List<PopularProduct> top(int limit) {
        return top.stream()
                .sorted(Comparator.comparingLong(PopularProduct::views)
                        .reversed())
                .limit(limit)
                .toList();
    }
}
