package com.punch.shop.order.dto;

import com.punch.shop.order.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotNull(message = "배송지를 선택해주세요")
    private Long addressId;

    @NotNull(message = "결제 수단을 선택해주세요")
    private PaymentMethod paymentMethod;
}
