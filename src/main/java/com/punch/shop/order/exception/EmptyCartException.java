package com.punch.shop.order.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("장바구니가 비어있습니다.");
    }
}
