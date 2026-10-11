package com.ecommerce.catalog.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/search")
public class SearchController {

    private final ProductSearchService service;

    public SearchController(ProductSearchService service) {
        this.service = service;
    }

    /** after: id terakhir dari halaman sebelumnya (0 = dari awal). */
    @GetMapping
    public SearchSlice search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") long after,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(q, after, size);
    }
}
