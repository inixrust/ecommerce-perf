package com.ecommerce.order.support;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

/** Dua server WireMock menggantikan catalog-service dan pricing. */
@TestConfiguration(proxyBeanMethods = false)
public class WireMockConfig {

    private final HttpObserver catalogObserver =
            new HttpObserver("catalog-observer");
    private final HttpObserver pricingObserver =
            new HttpObserver("pricing-observer");

    @Bean(destroyMethod = "stop")
    WireMockServer catalogStub() {
        var server = new WireMockServer(options().dynamicPort()
                .extensions(catalogObserver));
        server.start();
        return server;
    }

    @Bean(destroyMethod = "stop")
    WireMockServer pricingStub() {
        var server = new WireMockServer(options().dynamicPort()
                .extensions(pricingObserver));
        server.start();
        return server;
    }

    @Bean
    Stubs stubs(WireMockServer catalogStub, WireMockServer pricingStub) {
        return new Stubs(catalogStub, pricingStub, catalogObserver,
                pricingObserver);
    }

    @Bean
    DynamicPropertyRegistrar stubUrls(WireMockServer catalogStub,
            WireMockServer pricingStub) {
        return registry -> {
            registry.add("clients.catalog.base-url", catalogStub::baseUrl);
            registry.add("clients.pricing.base-url", pricingStub::baseUrl);
        };
    }
}
