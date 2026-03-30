package com.punch.shop.order.exception;

import com.punch.shop.common.exception.BadRequestException;

public class OrderCancelNotAllowedException extends BadRequestException {

    public OrderCancelNotAllowedException(Long orderId) {
        super("취소할 수 없는 주문 상태입니다: " + orderId);
    }
}
