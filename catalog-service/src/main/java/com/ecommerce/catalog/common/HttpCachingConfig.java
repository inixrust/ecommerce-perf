package com.ecommerce.catalog.common;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * ETag untuk respons besar yang jarang berubah. ShallowEtagHeaderFilter
 * tetap menjalankan controller dan menyusun JSON lengkap, lalu menghitung
 * hash-nya: yang dihemat adalah bandwidth (304 tanpa body), bukan kerja
 * server.
 */
@Configuration(proxyBeanMethods = false)
class HttpCachingConfig {

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        var registration = new FilterRegistrationBean<>(
                new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/v1/categories/tree");
        return registration;
    }
}
