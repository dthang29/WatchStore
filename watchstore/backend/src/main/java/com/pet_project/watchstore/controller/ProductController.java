package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.dto.ProductResponse;
import com.pet_project.watchstore.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** GET /api/products?page=0&size=12&keyword=seiko&sort=createdAt,desc */
    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findAll(keyword, pageable);
    }

    /** Sản phẩm được tạo mới hoặc nhập thêm hàng trong 30 ngày gần nhất. */
    @GetMapping("/new-arrivals")
    public PageResponse<ProductResponse> findNewArrivals(
            @PageableDefault(size = 30, sort = "lastRestockedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findNewArrivals(pageable);
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable Integer productId) {
        return productService.findById(productId);
    }
}
