package com.punch.shop.cart.exception;

import com.punch.shop.common.exception.NotFoundException;

public class CartNotFoundException extends NotFoundException {

    public CartNotFoundException() {
        super("장바구니를 찾을 수 없습니다.");
    }
}
