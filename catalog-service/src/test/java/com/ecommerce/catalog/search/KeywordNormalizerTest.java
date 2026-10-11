package com.ecommerce.catalog.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Kebenaran normalisasi; harus tetap hijau setelah optimasi. */
class KeywordNormalizerTest {

    private final KeywordNormalizer normalizer = new KeywordNormalizer();

    @ParameterizedTest
    @CsvSource({
            "Kaos, kaos",
            "KATUN!, katun",
            "Kafé, kafe",
            "naïve, naive",
            "(premium), premium",
            "USB-C, usbc",
            "x2, x2",
    })
    void normalizesWord(String word, String expected) {
        assertThat(normalizer.normalize(word)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"a", "!", "é", "''"})
    void rejectsTooShortOrEmptyWord(String word) {
        assertThat(normalizer.normalize(word)).isNull();
    }

    @Test
    void splitsTextIntoKeywords() {
        assertThat(normalizer.keywords("Kaos  Polos, KATUN 100% a"))
                .containsExactly("kaos", "polos", "katun", "100");
    }
}
