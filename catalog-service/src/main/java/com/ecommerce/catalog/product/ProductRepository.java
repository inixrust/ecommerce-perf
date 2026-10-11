package com.ecommerce.catalog.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
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

    // Satu produk: aman mengambil kategori dan koleksi gambar dengan
    // JOIN FETCH karena tidak ada paginasi.
    @Query("""
            select p from Product p
            join fetch p.category
            left join fetch p.images
            where p.id = :id""")
    Optional<Product> findDetailById(Long id);

    @Query("""
            select p from Product p
            where lower(p.name) like :pattern and p.id > :after
            order by p.id""")
    List<Product> searchByNameAfter(String pattern, long after,
            Limit limit);
}
