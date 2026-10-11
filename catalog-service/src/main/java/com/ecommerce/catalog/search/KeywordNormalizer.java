package com.ecommerce.catalog.search;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Mengubah teks bebas menjadi kata kunci: huruf kecil, tanpa diakritik
 * ("Kafé" menjadi "kafe"), tanpa tanda baca, minimal dua karakter.
 * Dipakai untuk kata kunci pencarian dan untuk mencocokkan kata di
 * deskripsi produk saat membuat snippet.
 */
@Component
public class KeywordNormalizer {

    public List<String> keywords(String text) {
        List<String> result = new ArrayList<>();
        for (String word : text.split(" ")) {
            String keyword = normalize(word);
            if (keyword != null) {
                result.add(keyword);
            }
        }
        return result;
    }

    /** Mengembalikan null bila kata tidak layak menjadi kata kunci. */
    public String normalize(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        var decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String withoutMarks = decomposed.replaceAll("\\p{M}", "");
        String cleaned = withoutMarks.replaceAll("[^a-z0-9]", "");
        if (!cleaned.matches("[a-z0-9]{2,}")) {
            return null;
        }
        return cleaned;
    }
}
