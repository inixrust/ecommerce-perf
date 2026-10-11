package com.ecommerce.catalog.support;

import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Mengosongkan semua cache sebelum setiap test. Data uji dimuat ulang per
 * kelas test, sedangkan cache (Redis, Caffeine) hidup sepanjang konteks
 * Spring yang dipakai bersama; tanpa ini, test bisa membaca isi lama.
 */
public class ClearCachesListener implements TestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        testContext.getApplicationContext()
                .getBeanProvider(CacheManager.class)
                .forEach(manager -> manager.getCacheNames().forEach(
                        name -> manager.getCache(name).clear()));
    }
}
