package com.ecommerce.catalog.category;

import com.ecommerce.catalog.common.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    /** Menyusun pohon kategori dari satu query, lalu di memori. */
    // TODO (Latihan 4.2)
    @Transactional(readOnly = true)
    public List<CategoryNode> tree() {
        List<Category> all = categories.findAll();
        Map<Long, CategoryNode> nodes = new HashMap<>();
        for (Category c : all) {
            nodes.put(c.getId(), new CategoryNode(c.getId(), c.getName()));
        }
        List<CategoryNode> roots = new ArrayList<>();
        for (Category c : all) {
            CategoryNode node = nodes.get(c.getId());
            // parent sudah dimuat oleh findAll(), jadi tanpa query baru
            if (c.getParent() == null) {
                roots.add(node);
            } else {
                nodes.get(c.getParent().getId()).children().add(node);
            }
        }
        return roots;
    }

    // TODO (Latihan 4.2)
    @Transactional
    public CategoryNode create(CreateCategoryRequest request) {
        Category parent = null;
        if (request.parentId() != null) {
            parent = categories.findById(request.parentId())
                    .orElseThrow(() -> new NotFoundException(
                            "Category", request.parentId()));
        }
        Category saved = categories.save(
                new Category(request.name(), parent));
        return new CategoryNode(saved.getId(), saved.getName());
    }
}
