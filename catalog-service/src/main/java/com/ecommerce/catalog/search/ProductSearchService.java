package com.ecommerce.catalog.search;

import com.ecommerce.catalog.common.PageResponse;
import com.ecommerce.catalog.product.Product;
import com.ecommerce.catalog.product.ProductRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    // TODO (Latihan 2.5)
    public PageResponse<SearchHit> search(String q, int page, int size) {
        List<String> keywords = normalizer.keywords(q);
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException(
                    "q must contain at least one keyword");
        }
        // urutan kata kunci dipertahankan: "kaos polos" -> %kaos%polos%
        String pattern = "%" + String.join("%", keywords) + "%";
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Product> result = products.searchByName(pattern, pageable);
        return PageResponse.of(result.map(p -> toHit(p, keywords)));
    }

    private SearchHit toHit(Product p, List<String> keywords) {
        String snippet = snippets.build(p.getDescription(), keywords);
        return new SearchHit(p.getId(), p.getSku(), p.getName(),
                p.getPrice(), snippet);
    }
}
