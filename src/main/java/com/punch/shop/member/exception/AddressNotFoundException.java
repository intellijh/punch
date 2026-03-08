package com.punch.shop.member.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long addressId) {
        super("배송지를 찾을 수 없습니다: " + addressId);
    }
}
