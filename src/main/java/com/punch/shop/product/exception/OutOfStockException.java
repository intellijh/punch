package com.punch.shop.product.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long productId, String productName) {
        super("재고가 부족합니다: " + productName + " (ID: " + productId + ")");
    }
}
