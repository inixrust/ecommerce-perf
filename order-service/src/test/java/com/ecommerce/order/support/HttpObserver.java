package com.ecommerce.order.support;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Ekstensi WireMock yang berjalan di dalam JVM test. Dipanggil saat stub
 * mulai melayani request, ketika aplikasi masih menunggu jawaban, jadi
 * test dapat memeriksa keadaan aplikasi pada saat itu. Juga menghitung
 * jumlah maksimum request yang dilayani bersamaan.
 */
public class HttpObserver implements ServeEventListener {

    private final String name;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger maxInFlight = new AtomicInteger();
    private volatile Consumer<String> onRequest = request -> {
    };

    HttpObserver(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void beforeMatch(ServeEvent event, Parameters parameters) {
        int now = inFlight.incrementAndGet();
        maxInFlight.accumulateAndGet(now, Math::max);
        var request = event.getRequest();
        onRequest.accept(request.getMethod() + " " + request.getUrl());
    }

    @Override
    public void afterComplete(ServeEvent event, Parameters parameters) {
        inFlight.decrementAndGet();
    }

    void reset() {
        onRequest = request -> {
        };
        maxInFlight.set(0);
    }

    void onRequest(Consumer<String> callback) {
        onRequest = callback;
    }

    int maxInFlight() {
        return maxInFlight.get();
    }
}
