package com.pet_project.watchstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.pet_project.watchstore.common.constants.MovementType;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false,
            columnDefinition = "enum('QUARTZ','AUTOMATIC','SMART')")
    private MovementType movementType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_restocked_at", nullable = false)
    private LocalDateTime lastRestockedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
        if (lastRestockedAt == null) lastRestockedAt = createdAt;
    }

    public static Product create(Category category, String name, String brand, String description,
                                 MovementType movementType, BigDecimal price, int stock, String imageUrl) {
        Product product = new Product();
        product.update(category, name, brand, description, movementType, price, stock, imageUrl);
        return product;
    }

    public void update(Category category, String name, String brand, String description,
                       MovementType movementType, BigDecimal price, int stock, String imageUrl) {
        this.category = category;
        this.name = name.trim();
        this.brand = brand.trim();
        this.description = blankToNull(description);
        this.movementType = movementType;
        this.price = price;
        if (stock > this.stock) lastRestockedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.stock = stock;
        this.imageUrl = blankToNull(imageUrl);
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void reserveStock(int quantity) {
        if (quantity <= 0 || quantity > stock) {
            throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ");
        }
        stock -= quantity;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
