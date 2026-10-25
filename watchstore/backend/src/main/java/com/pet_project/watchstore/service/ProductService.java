package com.pet_project.watchstore.service;

import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.dto.ProductResponse;
import com.pet_project.watchstore.dto.ProductUpsertRequest;
import com.pet_project.watchstore.dto.CategoryResponse;
import com.pet_project.watchstore.entity.Category;
import com.pet_project.watchstore.entity.Product;
import com.pet_project.watchstore.repository.CategoryRepository;
import com.pet_project.watchstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Product> products = productRepository.findPublicProducts(normalizedKeyword, pageable);

        return PageResponse.from(products.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Integer productId) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findNewArrivals(Pageable pageable) {
        LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minusMonths(1);
        return PageResponse.from(productRepository.findNewArrivals(since, pageable).map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllForAdmin(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Product> products = productRepository.findAdminProducts(normalizedKeyword, pageable);
        return PageResponse.from(products.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(ProductUpsertRequest request) {
        Category category = findCategory(request.categoryId());
        Product product = Product.create(category, request.name(), request.brand(), request.description(),
                request.movementType(), request.price(), request.stock(), request.imageUrl());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Integer productId, ProductUpsertRequest request) {
        Product product = findProduct(productId);
        product.update(findCategory(request.categoryId()), request.name(), request.brand(), request.description(),
                request.movementType(), request.price(), request.stock(), request.imageUrl());
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(Integer productId) {
        findProduct(productId).deactivate();
    }

    @Transactional
    public void activate(Integer productId) {
        findProduct(productId).activate();
    }

    private Product findProduct(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    private Category findCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại"));
    }
}
