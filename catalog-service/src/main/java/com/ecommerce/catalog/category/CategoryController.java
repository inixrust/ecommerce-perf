package com.ecommerce.catalog.category;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    public List<CategoryNode> tree() {
        return service.tree();
    }

    @PostMapping
    public ResponseEntity<CategoryNode> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryNode created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + created.id()))
                .body(created);
    }
}
