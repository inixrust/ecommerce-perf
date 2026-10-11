package com.ecommerce.catalog.search;

import com.ecommerce.catalog.product.Product;
import com.ecommerce.catalog.product.ProductRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

@Service
public class ProductSearchService {

    private final ProductRepository products;
    private final KeywordNormalizer normalizer;
    private final SnippetBuilder snippets;

    public ProductSearchService(ProductRepository products,
            KeywordNormalizer normalizer, SnippetBuilder snippets) {
        this.products = products;
        this.normalizer = normalizer;
        this.snippets = snippets;
    }

    /**
     * Keyset pagination: lanjut dari id terakhir halaman sebelumnya,
     * sehingga biaya query tidak tumbuh seiring kedalaman halaman dan
     * tidak perlu count(*) di setiap halaman.
     */
    public SearchSlice search(String q, long after, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        List<String> keywords = normalizer.keywords(q);
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException(
                    "q must contain at least one keyword");
        }
        // urutan kata kunci dipertahankan: "kaos polos" -> %kaos%polos%
        String pattern = "%" + String.join("%", keywords) + "%";
        // satu baris ekstra hanya untuk mengetahui ada halaman berikutnya
        List<Product> rows = products.searchByNameAfter(
                pattern, after, Limit.of(size + 1));
        boolean hasNext = rows.size() > size;
        List<Product> page = hasNext ? rows.subList(0, size) : rows;
        Long nextAfter = hasNext ? page.getLast().getId() : null;
        List<SearchHit> hits = page.stream()
                .map(p -> toHit(p, keywords))
                .toList();
        return new SearchSlice(hits, nextAfter);
    }

    private SearchHit toHit(Product p, List<String> keywords) {
        String snippet = snippets.build(p.getDescription(), keywords);
        return new SearchHit(p.getId(), p.getSku(), p.getName(),
                p.getPrice(), snippet);
    }
}
