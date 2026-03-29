package com.punch.shop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank(message = "상품명을 입력해주세요")
    private String name;

    private String description;

    @NotNull(message = "가격을 입력해주세요")
    @Positive(message = "가격은 0보다 커야 합니다")
    private BigDecimal price;

    @NotNull(message = "재고를 입력해주세요")
    @PositiveOrZero(message = "재고는 0 이상이어야 합니다")
    private Integer stockQuantity;

    @NotNull(message = "카테고리를 선택해주세요")
    private Long categoryId;
}
