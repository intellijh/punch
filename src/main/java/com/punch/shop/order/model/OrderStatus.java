package com.punch.shop.order.model;

public enum OrderStatus {
    PAID("결제 완료"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
