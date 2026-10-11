package com.ecommerce.catalog.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.jfr.consumer.RecordingStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Verifikasi final challenge: penghitung view aman dipakai bersamaan dan
 * jalur record() tidak membuat thread menunggu monitor bersama.
 */
@Tag("challenge")
class ViewCounterTest {

    private static final int THREADS = 4;

    @Test
    void countsEveryViewUnderConcurrentLoad() throws Exception {
        var counter = new ViewCounter();

        try (var executor = Executors.newFixedThreadPool(THREADS)) {
            for (int t = 0; t < THREADS; t++) {
                executor.submit(() -> {
                    for (int i = 0; i < 1_000; i++) {
                        counter.record((long) (i % 10));
                    }
                });
            }
        }

        assertThat(counter.top(10))
                .as("setiap produk dilihat %d kali", THREADS * 100)
                .hasSize(10)
                .allMatch(p -> p.views() == THREADS * 100);
    }

    @Test
    void ranksMostViewedFirstThenLowestId() {
        var counter = new ViewCounter();
        record(counter, 7L, 3);
        record(counter, 5L, 1);
        record(counter, 9L, 3);
        record(counter, 2L, 2);

        assertThat(counter.top(3)).containsExactly(
                new PopularProduct(7L, 3),
                new PopularProduct(9L, 3),
                new PopularProduct(2L, 2));
    }

    @Test
    void recordingDoesNotWaitOnSharedMonitor() throws Exception {
        var counter = new ViewCounter();
        var waits = new AtomicInteger();

        try (var recording = new RecordingStream()) {
            recording.enable("jdk.JavaMonitorEnter")
                    .withThreshold(Duration.ZERO);
            recording.onEvent("jdk.JavaMonitorEnter", event -> {
                String monitor = event.getClass("monitorClass").getName();
                if (monitor.equals(ViewCounter.class.getName())) {
                    waits.incrementAndGet();
                }
            });
            recording.startAsync();

            try (var executor = Executors.newFixedThreadPool(THREADS)) {
                for (int t = 0; t < THREADS; t++) {
                    executor.submit(() -> {
                        for (int i = 0; i < 2_000; i++) {
                            counter.record((long) i);
                        }
                    });
                }
            }
            // menunggu sampai semua event yang terekam selesai diproses
            recording.stop();
        }

        assertThat(waits.get())
                .as("thread yang menunggu monitor ViewCounter")
                .isZero();
    }

    private static void record(ViewCounter counter, Long id, int times) {
        for (int i = 0; i < times; i++) {
            counter.record(id);
        }
    }
}
