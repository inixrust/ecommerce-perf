package com.ecommerce.catalog.category;

import java.util.ArrayList;
import java.util.List;

public record CategoryNode(
        Long id, String name, List<CategoryNode> children) {

    public CategoryNode(Long id, String name) {
        this(id, name, new ArrayList<>());
    }
}
