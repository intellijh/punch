package com.punch.shop.cart.exception;

import com.punch.shop.common.exception.NotFoundException;

public class CartItemNotFoundException extends NotFoundException {

    public CartItemNotFoundException(Long id) {
        super("장바구니 상품을 찾을 수 없습니다: " + id);
    }
}
