package com.punch.shop.order.model;

public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    NAVER_PAY("네이버페이"),
    KAKAO_PAY("카카오페이"),
    TOSS_PAY("토스페이");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
