package com.punch.shop.product.exception;

import com.punch.shop.common.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(Long id) {
        super("상품을 찾을 수 없습니다: " + id);
    }
}
