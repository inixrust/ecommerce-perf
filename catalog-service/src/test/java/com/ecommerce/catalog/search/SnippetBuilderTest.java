package com.ecommerce.catalog.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Kebenaran snippet; harus tetap hijau setelah optimasi. */
class SnippetBuilderTest {

    private final SnippetBuilder snippets =
            new SnippetBuilder(new KeywordNormalizer());

    @Test
    void highlightsMatchesAroundFirstHit() {
        String description = "satu dua tiga empat lima enam tujuh "
                + "delapan Kaos sembilan sepuluh katun! sebelas dua-belas "
                + "tiga-belas empat-belas lima-belas enam-belas";
        assertThat(snippets.build(description, List.of("kaos", "katun")))
                .isEqualTo("tiga empat lima enam tujuh delapan **Kaos** "
                        + "sembilan sepuluh **katun!** sebelas dua-belas");
    }

    @Test
    void startsFromBeginningWhenNothingMatches() {
        String description = "a b c d e f g h i j k l m n o";
        assertThat(snippets.build(description, List.of("kaos")))
                .isEqualTo("a b c d e f g h i j k l");
    }

    @Test
    void emptyDescriptionGivesEmptySnippet() {
        assertThat(snippets.build(null, List.of("kaos"))).isEmpty();
        assertThat(snippets.build(" ", List.of("kaos"))).isEmpty();
    }
}
