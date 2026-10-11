package com.ecommerce.catalog.category;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByIdIn(Collection<Long> ids);

    /** Nama kategori dari akar sampai kategori ini, dalam satu query. */
    @Query(nativeQuery = true, value = """
            WITH RECURSIVE path AS (
                SELECT id, name, parent_id, 0 AS depth
                FROM categories WHERE id = :id
                UNION ALL
                SELECT c.id, c.name, c.parent_id, p.depth + 1
                FROM categories c JOIN path p ON c.id = p.parent_id)
            SELECT name FROM path ORDER BY depth DESC""")
    List<String> pathNames(Long id);
}
