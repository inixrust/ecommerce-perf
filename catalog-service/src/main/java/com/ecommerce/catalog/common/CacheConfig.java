package com.ecommerce.catalog.common;

import com.ecommerce.catalog.product.ProductDetail;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cache terdistribusi (Redis) untuk data yang dibaca semua instance.
 *
 * order HIGHEST_PRECEDENCE: cache selalu membungkus transaksi, jadi
 * cache hit tidak membuka transaksi dan tidak meminjam koneksi, dan
 * eviction terjadi setelah commit. Tanpa order eksplisit, keduanya
 * LOWEST_PRECEDENCE dan urutannya bergantung pada urutan pendaftaran.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig {

    public static final String PRODUCT_DETAIL = "product-detail";
    public static final String CATEGORY_TREE = "category-tree";
    public static final String SEARCH_RESULTS = "search-results";
    public static final String LOCAL = "localCacheManager";

    @Bean
    @Primary
    RedisCacheManager cacheManager(RedisConnectionFactory connections,
            JsonMapper jsonMapper) {
        // JSON bertipe tetap: tanpa nama kelas di dalam data cache
        var detailSerializer =
                new JacksonJsonRedisSerializer<>(jsonMapper,
                        ProductDetail.class);
        var detail = RedisCacheConfiguration.defaultCacheConfig()
                // versi di key: bentuk DTO berubah, cukup naikkan v1
                .computePrefixWith(name -> "catalog:v1:" + name + "::")
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair
                        .fromSerializer(detailSerializer));
        return RedisCacheManager.builder(connections)
                .withCacheConfiguration(PRODUCT_DETAIL, detail)
                .enableStatistics()
                .transactionAware()
                .build();
    }

    /**
     * Cache lokal di heap setiap instance: tanpa network hop dan tanpa
     * serialisasi. Cocok untuk data kecil yang jarang berubah. Harga yang
     * dibayar: instance lain baru melihat perubahan setelah TTL habis.
     */
    @Bean(LOCAL)
    CaffeineCacheManager localCacheManager(
            @Value("${catalog.search.cache.maximum-size}")
            long searchMaximumSize) {
        var manager = new CaffeineCacheManager(CATEGORY_TREE);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        // kunci tak terbatas (q, after, size): batasi jumlah entry
        manager.registerCustomCache(SEARCH_RESULTS, Caffeine.newBuilder()
                .maximumSize(searchMaximumSize)
                .expireAfterWrite(Duration.ofMinutes(1))
                .recordStats()
                .build());
        return manager;
    }
}
