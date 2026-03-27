package com.punch.shop.order.model;

import com.punch.shop.common.model.BaseEntity;
import com.punch.shop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public static OrderItem create(Order order, Product product, int quantity) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(quantity)
                .build();
    }
}
