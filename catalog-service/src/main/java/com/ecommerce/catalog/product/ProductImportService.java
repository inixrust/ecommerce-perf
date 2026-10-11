package com.ecommerce.catalog.product;

import com.ecommerce.catalog.category.Category;
import com.ecommerce.catalog.category.CategoryRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImportService {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductImportService(ProductRepository products,
            CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Transactional
    public ImportResult importAll(List<NewProduct> request) {
        Set<Long> categoryIds = request.stream()
                .map(NewProduct::categoryId)
                .collect(Collectors.toSet());
        if (categories.countByIdIn(categoryIds) != categoryIds.size()) {
            throw new IllegalArgumentException(
                    "Unknown categoryId in import request");
        }
        List<Product> toSave = request.stream()
                .map(this::toEntity)
                .toList();
        products.saveAll(toSave);
        return new ImportResult(toSave.size());
    }

    private Product toEntity(NewProduct p) {
        // getReferenceById: proxy tanpa SELECT, cukup untuk foreign key
        Category category = categories.getReferenceById(p.categoryId());
        return new Product(p.sku(), p.name(), p.description(), p.price(),
                p.status(), category);
    }
}
