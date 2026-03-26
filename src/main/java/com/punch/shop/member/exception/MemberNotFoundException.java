package com.punch.shop.member.exception;

import com.punch.shop.common.exception.NotFoundException;

public class MemberNotFoundException extends NotFoundException {

    public MemberNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다: " + email);
    }

    public MemberNotFoundException(Long memberId) {
        super("사용자를 찾을 수 없습니다: " + memberId);
    }
}
