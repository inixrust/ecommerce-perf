package com.ecommerce.catalog.search;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Membuat potongan deskripsi di sekitar kata pertama yang cocok dengan
 * kata kunci. Kata yang cocok diapit tanda bintang dua.
 */
@Component
public class SnippetBuilder {

    static final int WINDOW = 12;

    private final KeywordNormalizer normalizer;

    public SnippetBuilder(KeywordNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public String build(String description, List<String> keywords) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String[] words = description.split(" ");
        int match = -1;
        for (int i = 0; i < words.length; i++) {
            String keyword = normalizer.normalize(words[i]);
            if (keyword != null && keywords.contains(keyword)) {
                match = i;
                break;
            }
        }
        int start = Math.max(0, match - WINDOW / 2);
        int end = Math.min(words.length, start + WINDOW);
        // TODO (Latihan 1.1)
        String snippet = "";
        for (int i = start; i < end; i++) {
            String keyword = normalizer.normalize(words[i]);
            boolean hit = keyword != null && keywords.contains(keyword);
            snippet = snippet + (hit ? "**" + words[i] + "**" : words[i]);
            if (i < end - 1) {
                snippet = snippet + " ";
            }
        }
        return snippet;
    }
}
