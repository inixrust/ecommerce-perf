package com.ecommerce.catalog.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // Kategori (to-one) ikut di-JOIN pada query halaman. Aman dengan
    // paginasi: satu baris hasil tetap satu produk.
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Query("select p from Product p where lower(p.name) like :pattern")
    Page<Product> searchByName(String pattern, Pageable pageable);
}
