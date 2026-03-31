package com.punch.shop.product.model;

import com.punch.shop.common.model.BaseEntity;
import com.punch.shop.product.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    private String thumbnailImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private int orderCount = 0;

    public void reduceStock(int quantity) {
        if (stockQuantity < quantity) {
            throw new OutOfStockException(id, name);
        }
        stockQuantity -= quantity;
    }

    public void restoreStock(int quantity) {
        stockQuantity += quantity;
    }

    public void increaseOrderCount(int quantity) {
        this.orderCount += quantity;
    }

    public void decreaseOrderCount(int quantity) {
        this.orderCount -= quantity;
    }
}
