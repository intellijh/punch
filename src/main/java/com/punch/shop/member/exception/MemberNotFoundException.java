package com.punch.shop.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다: " + email);
    }
}
