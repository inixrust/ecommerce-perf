package com.ecommerce.catalog.product;

import org.springframework.data.jpa.domain.Specification;

/** Filter opsional: predicate null diabaikan oleh Spring Data. */
final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> status == null
                ? null
                : cb.equal(root.get("status"), status);
    }
}
