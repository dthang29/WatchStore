package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.dto.CategoryResponse;
import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.dto.ProductResponse;
import com.pet_project.watchstore.dto.ProductUpsertRequest;
import com.pet_project.watchstore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findAllForAdmin(keyword, pageable);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> findCategories() {
        return productService.findCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductUpsertRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable Integer productId, @Valid @RequestBody ProductUpsertRequest request) {
        return productService.update(productId, request);
    }

    /** Soft delete: giữ lại bản ghi cho lịch sử đơn hàng, chỉ ẩn khỏi cửa hàng. */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Integer productId) {
        productService.deactivate(productId);
    }

    @PatchMapping("/{productId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Integer productId) {
        productService.activate(productId);
    }
}
