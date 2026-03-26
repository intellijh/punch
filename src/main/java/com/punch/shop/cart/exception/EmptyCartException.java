package com.punch.shop.cart.exception;

import com.punch.shop.common.exception.BadRequestException;

public class EmptyCartException extends BadRequestException {

    public EmptyCartException() {
        super("장바구니가 비어있습니다.");
    }
}
