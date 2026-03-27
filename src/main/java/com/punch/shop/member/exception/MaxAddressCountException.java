package com.punch.shop.member.exception;

import com.punch.shop.common.exception.BadRequestException;

public class MaxAddressCountException extends BadRequestException {

    public MaxAddressCountException(int maxCount) {
        super("배송지는 최대 " + maxCount + "개까지 등록할 수 있습니다.");
    }
}
