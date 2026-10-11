package com.ecommerce.catalog.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.StringJoiner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Verifikasi Latihan 1.1. Yang diukur adalah jumlah byte yang
 * dialokasikan per panggilan, bukan waktu: angka alokasi tidak
 * bergantung pada kecepatan mesin, jadi hasilnya hampir sama di setiap
 * laptop.
 */
@Tag("lab-1.1")
class SnippetAllocationTest {

    /**
     * Batas alokasi per panggilan untuk deskripsi 220 kata. Dipilih di
     * antara kondisi awal dan kondisi setelah perbaikan (lihat modul).
     */
    private static final long BUDGET_BYTES = 192 * 1024;

    private static final String[] WORDS = {"nyaman", "dipakai", "harian",
        "tahan", "lama", "mudah", "dibersihkan", "cocok", "untuk",
        "rumah", "kantor", "perjalanan", "dengan", "desain", "rapi"};

    private final SnippetBuilder snippets =
            new SnippetBuilder(new KeywordNormalizer());

    @Test
    void snippetStaysWithinAllocationBudget() {
        String description = description(220);
        List<String> keywords = List.of("kaos");

        // warmup agar JIT selesai mengompilasi jalur panas
        for (int i = 0; i < 2_000; i++) {
            snippets.build(description, keywords);
        }

        var threads = (com.sun.management.ThreadMXBean)
                ManagementFactory.getThreadMXBean();
        int calls = 500;
        long before = threads.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < calls; i++) {
            snippets.build(description, keywords);
        }
        long perCall = (threads.getCurrentThreadAllocatedBytes() - before)
                / calls;

        assertThat(perCall)
                .as("byte per panggilan SnippetBuilder.build()")
                .isLessThan(BUDGET_BYTES);
    }

    private static String description(int words) {
        var joiner = new StringJoiner(" ");
        for (int i = 0; i < words; i++) {
            joiner.add(WORDS[(i * 7) % WORDS.length]);
        }
        return joiner.toString();
    }
}
