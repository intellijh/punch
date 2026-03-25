package com.punch.shop.cart.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long id) {
        super("장바구니 상품을 찾을 수 없습니다: " + id);
    }
}
