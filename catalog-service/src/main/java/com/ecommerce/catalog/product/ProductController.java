package com.ecommerce.catalog.product;

import com.ecommerce.catalog.common.PageResponse;
import com.ecommerce.catalog.common.Paging;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;
    private final ProductImportService importService;
    private final ViewCounter views;

    public ProductController(ProductService service,
            ProductImportService importService, ViewCounter views) {
        this.service = service;
        this.importService = importService;
        this.views = views;
    }

    @GetMapping
    public PageResponse<ProductSummary> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Min(1)
            @Max(Paging.MAX_SIZE) int size) {
        return service.list(categoryId, status, page, size);
    }

    @GetMapping("/{id:\\d+}")
    public ProductDetail get(@PathVariable Long id) {
        ProductDetail detail = service.get(id);
        views.record(id);
        return detail;
    }

    @GetMapping("/popular")
    public List<PopularProduct> popular(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50)
            int limit) {
        return views.top(limit);
    }

    @PutMapping("/{id:\\d+}")
    public ProductDetail update(@PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/import")
    public ImportResult importProducts(
            @Valid @RequestBody ImportProductsRequest request) {
        return importService.importAll(request.products());
    }
}
