package com.punch.shop.order.dto;

import com.punch.shop.order.model.Order;
import com.punch.shop.order.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderListItemResponse {

    private Long orderId;
    private OrderStatus status;
    private String statusDescription;
    private BigDecimal totalPrice;
    private int itemCount;
    private String firstItemName;
    private LocalDateTime createdAt;

    public static OrderListItemResponse from(Order order) {
        return OrderListItemResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .totalPrice(order.getTotalPrice())
                .itemCount(order.getItems().size())
                .firstItemName(order.getItems().isEmpty() ? "" : order.getItems().get(0).getProductName())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
