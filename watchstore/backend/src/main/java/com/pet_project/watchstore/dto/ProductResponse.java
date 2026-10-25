package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Response công khai; không trả về entity JPA trực tiếp để tránh lỗi lazy-loading. */
public record ProductResponse(
        Integer id,
        Integer categoryId,
        String categoryName,
        String name,
        String brand,
        String description,
        String movementType,
        BigDecimal price,
        int stock,
        boolean active,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime lastRestockedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getMovementType().name(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getImageUrl(),
                product.getCreatedAt(),
                product.getLastRestockedAt());
    }
}
