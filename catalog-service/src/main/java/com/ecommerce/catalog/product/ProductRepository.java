package com.ecommerce.catalog.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

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
