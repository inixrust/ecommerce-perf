package com.ecommerce.order.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.jfr.consumer.RecordingStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verifikasi Latihan 4.3. Di Java 21, virtual thread yang menunggu I/O
 * di dalam blok synchronized tidak bisa dilepas dari carrier thread-nya
 * (pinned). JFR mencatatnya sebagai event jdk.VirtualThreadPinned.
 */
@IntegrationTest
@Tag("lab-4.3")
class TokenPinningTest {

    @Autowired
    PricingTokenProvider tokens;

    @Autowired
    Stubs stubs;

    @Test
    void tokenRefreshDoesNotPinCarrierThreads() throws Exception {
        stubs.reset();
        // TTL 0: setiap panggilan memperbarui token; endpoint token
        // menjawab dalam 100 ms, di atas ambang event (20 ms)
        stubs.token(0, 100);
        var pinned = new AtomicInteger();

        try (var recording = new RecordingStream()) {
            recording.enable("jdk.VirtualThreadPinned")
                    .withThreshold(Duration.ofMillis(20));
            recording.onEvent("jdk.VirtualThreadPinned",
                    event -> pinned.incrementAndGet());
            recording.startAsync();

            try (var executor =
                    Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 4; i++) {
                    executor.submit(tokens::token);
                }
            }
            // menunggu sampai semua event yang terekam selesai diproses
            recording.stop();
        }

        assertThat(pinned.get())
                .as("event jdk.VirtualThreadPinned saat token diperbarui")
                .isZero();
    }
}
