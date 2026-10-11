package com.ecommerce.catalog.product;

import com.ecommerce.catalog.common.NotFoundException;
import com.ecommerce.catalog.common.PageResponse;
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

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public PageResponse<ProductSummary> list(Long categoryId,
            ProductStatus status, int page, int size) {
        var filter = Specification.allOf(
                ProductSpecifications.inCategory(categoryId),
                ProductSpecifications.hasStatus(status));
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Product> result = products.findAll(filter, pageable);
        log.debug("Products loaded: " + result.getContent());
        return PageResponse.of(result.map(ProductService::toSummary));
    }

    public Product get(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    @Transactional
    public Product update(Long id, UpdateProductRequest request) {
        Product product = get(id);
        product.update(request.name(), request.price(), request.status());
        return product;
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
