package com.punch.shop.cart.dto;

import com.punch.shop.cart.model.Cart;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponse {

    private List<CartItemResponse> items;
    private BigDecimal totalPrice;

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        return CartResponse.builder()
                .items(items)
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    public static CartResponse empty() {
        return CartResponse.builder()
                .items(List.of())
                .totalPrice(BigDecimal.ZERO)
                .build();
    }
}
