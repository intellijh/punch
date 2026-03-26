package com.punch.shop.order.exception;

import com.punch.shop.common.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(Long orderId) {
        super("주문을 찾을 수 없습니다: " + orderId);
    }
}
