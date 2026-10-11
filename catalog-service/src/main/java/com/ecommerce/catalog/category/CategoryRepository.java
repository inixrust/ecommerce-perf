package com.ecommerce.catalog.category;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByIdIn(Collection<Long> ids);
}
