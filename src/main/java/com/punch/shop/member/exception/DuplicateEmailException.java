package com.punch.shop.member.exception;

import com.punch.shop.common.exception.BadRequestException;

public class DuplicateEmailException extends BadRequestException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
