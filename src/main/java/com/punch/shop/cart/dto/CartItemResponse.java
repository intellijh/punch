package com.punch.shop.cart.dto;

import com.punch.shop.cart.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String thumbnailImageUrl;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
    private boolean checked;
    private int stockQuantity;
    private boolean outOfStock;

    public static CartItemResponse from(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .thumbnailImageUrl(item.getProduct().getThumbnailImageUrl())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .checked(item.isChecked())
                .stockQuantity(item.getProduct().getStockQuantity())
                .outOfStock(item.getProduct().getStockQuantity() == 0)
                .build();
    }
}
