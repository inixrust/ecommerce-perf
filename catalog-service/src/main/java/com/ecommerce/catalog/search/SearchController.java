package com.ecommerce.catalog.search;

import com.ecommerce.catalog.common.Paging;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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
            @RequestParam(defaultValue = "0") @PositiveOrZero long after,
            @RequestParam(defaultValue = "20") @Min(1)
            @Max(Paging.MAX_SIZE) int size) {
        return service.search(q, after, size);
    }
}
