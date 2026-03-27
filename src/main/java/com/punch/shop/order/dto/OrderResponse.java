package com.punch.shop.order.dto;

import com.punch.shop.common.util.PhoneUtils;
import com.punch.shop.order.model.Order;
import com.punch.shop.order.model.OrderStatus;
import com.punch.shop.order.model.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private OrderStatus status;
    private String statusDescription;
    private PaymentMethod paymentMethod;
    private String paymentMethodDescription;
    private String recipientName;
    private String phone;
    private String zipCode;
    private String address;
    private String addressDetail;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .paymentMethod(order.getPaymentMethod())
                .paymentMethodDescription(order.getPaymentMethod().getDescription())
                .recipientName(order.getRecipientName())
                .phone(PhoneUtils.format(order.getPhone()))
                .zipCode(order.getZipCode())
                .address(order.getAddress())
                .addressDetail(order.getAddressDetail())
                .items(order.getItems().stream().map(OrderItemResponse::from).toList())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
