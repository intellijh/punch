package com.punch.shop.product.exception;

import com.punch.shop.common.exception.BadRequestException;

public class OutOfStockException extends BadRequestException {

    public OutOfStockException(Long productId, String productName) {
        super("재고가 부족합니다: " + productName + " (ID: " + productId + ")");
    }
}
