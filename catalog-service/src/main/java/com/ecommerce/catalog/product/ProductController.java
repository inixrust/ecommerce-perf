package com.ecommerce.catalog.product;

import com.ecommerce.catalog.common.PageResponse;
import jakarta.validation.Valid;
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

    public ProductController(ProductService service,
            ProductImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    public PageResponse<ProductSummary> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(categoryId, status, page, size);
    }

    @GetMapping("/{id:\\d+}")
    public ProductDetail get(@PathVariable Long id) {
        return service.get(id);
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
