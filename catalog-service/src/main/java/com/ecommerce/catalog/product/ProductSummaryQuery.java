package com.ecommerce.catalog.product;

import com.ecommerce.catalog.category.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * DTO projection untuk daftar produk: hanya kolom yang ditampilkan yang
 * di-SELECT, langsung menjadi ProductSummary tanpa entity. Thumbnail
 * diambil dengan subquery, jadi satu halaman cukup satu query + count.
 */
@Component
class ProductSummaryQuery {

    private final EntityManager em;

    ProductSummaryQuery(EntityManager em) {
        this.em = em;
    }

    Page<ProductSummary> find(Long categoryId, ProductStatus status,
            Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        var query = cb.createQuery(ProductSummary.class);
        Root<Product> p = query.from(Product.class);
        Join<Product, Category> c = p.join("category");
        Subquery<String> thumbnail = query.subquery(String.class);
        Root<ProductImage> image = thumbnail.from(ProductImage.class);
        thumbnail.select(image.get("url")).where(
                cb.equal(image.get("product"), p),
                cb.equal(image.get("position"), 1));
        query.select(cb.construct(ProductSummary.class,
                        p.get("id"), p.get("sku"), p.get("name"),
                        p.get("price"), p.get("status"), c.get("name"),
                        thumbnail))
                .where(filters(cb, p, categoryId, status))
                .orderBy(cb.asc(p.get("id")));
        List<ProductSummary> content = em.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        var countQuery = cb.createQuery(Long.class);
        Root<Product> counted = countQuery.from(Product.class);
        countQuery.select(cb.count(counted))
                .where(filters(cb, counted, categoryId, status));
        long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private static Predicate[] filters(CriteriaBuilder cb,
            Root<Product> p, Long categoryId, ProductStatus status) {
        List<Predicate> predicates = new ArrayList<>();
        if (categoryId != null) {
            predicates.add(cb.equal(p.get("category").get("id"),
                    categoryId));
        }
        if (status != null) {
            predicates.add(cb.equal(p.get("status"), status));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
