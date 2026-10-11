package com.ecommerce.catalog.product;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

/**
 * Menghitung berapa kali detail produk dilihat sejak service start, untuk
 * halaman "produk populer".
 *
 * Jalur panas (setiap detail produk) hanya menaikkan satu LongAdder:
 * tanpa lock bersama dan tanpa biaya yang tumbuh seiring jumlah produk.
 * Peringkat dihitung saat endpoint populer dipanggil, yang jauh lebih
 * jarang daripada detail produk.
 */
@Component
public class ViewCounter {

    // urutan naik = peringkat terburuk lebih dulu (kepala min-heap)
    private static final Comparator<PopularProduct> WORST_FIRST =
            Comparator.comparingLong(PopularProduct::views)
                    .thenComparing(PopularProduct::productId,
                            Comparator.reverseOrder());

    private final Map<Long, LongAdder> views = new ConcurrentHashMap<>();

    public void record(Long productId) {
        views.computeIfAbsent(productId, id -> new LongAdder())
                .increment();
    }

    /**
     * Peringkat dari snapshot hitungan saat ini. View yang masuk selama
     * perhitungan boleh ikut atau tidak: angka populer adalah perkiraan.
     * Min-heap berukuran limit: O(n log limit), tanpa mengurutkan semua.
     */
    public List<PopularProduct> top(int limit) {
        var best = new PriorityQueue<>(limit + 1, WORST_FIRST);
        views.forEach((id, count) -> {
            best.add(new PopularProduct(id, count.sum()));
            if (best.size() > limit) {
                best.poll();
            }
        });
        return best.stream().sorted(WORST_FIRST.reversed()).toList();
    }
}
