package com.punch.shop.member.exception;

import com.punch.shop.common.exception.NotFoundException;

public class AddressNotFoundException extends NotFoundException {

    public AddressNotFoundException(Long addressId) {
        super("배송지를 찾을 수 없습니다: " + addressId);
    }
}
