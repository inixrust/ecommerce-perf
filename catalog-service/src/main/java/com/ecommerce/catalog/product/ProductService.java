package com.ecommerce.catalog.product;

import com.ecommerce.catalog.category.CategoryRepository;
import com.ecommerce.catalog.common.NotFoundException;
import com.ecommerce.catalog.common.PageResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final Logger log =
            LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products,
            CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    // Tanpa OSIV, relasi LAZY hanya bisa dimuat di dalam transaksi.
    // readOnly: Hibernate tidak menyimpan snapshot untuk dirty checking
    // dan tidak melakukan flush di akhir transaksi.
    @Transactional(readOnly = true)
    public PageResponse<ProductSummary> list(Long categoryId,
            ProductStatus status, int page, int size) {
        var filter = Specification.allOf(
                ProductSpecifications.inCategory(categoryId),
                ProductSpecifications.hasStatus(status));
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Product> result = products.findAll(filter, pageable);
        // placeholder {}: pesan hanya dirakit bila level DEBUG aktif
        log.debug("Loaded {} products (category={}, status={})",
                result.getNumberOfElements(), categoryId, status);
        return PageResponse.of(result.map(ProductService::toSummary));
    }

    @Transactional(readOnly = true)
    public ProductDetail get(Long id) {
        return toDetail(load(id));
    }

    @Transactional
    public ProductDetail update(Long id, UpdateProductRequest request) {
        Product product = load(id);
        product.update(request.name(), request.price(), request.status());
        return toDetail(product);
    }

    private Product load(Long id) {
        return products.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    private ProductDetail toDetail(Product p) {
        List<String> path = categories.pathNames(p.getCategory().getId());
        List<ProductDetail.Image> images = p.getImages().stream()
                .map(i -> new ProductDetail.Image(i.getUrl(),
                        i.getPosition()))
                .toList();
        return new ProductDetail(p.getId(), p.getSku(), p.getName(),
                p.getDescription(), p.getPrice(), p.getStatus(),
                new ProductDetail.CategoryRef(p.getCategory().getId(),
                        p.getCategory().getName()),
                path, images, p.getCreatedAt(), p.getUpdatedAt());
    }

    private static ProductSummary toSummary(Product p) {
        String thumbnail = p.getImages().isEmpty()
                ? null
                : p.getImages().getFirst().getUrl();
        return new ProductSummary(p.getId(), p.getSku(), p.getName(),
                p.getPrice(), p.getStatus(), p.getCategory().getName(),
                thumbnail);
    }
}
