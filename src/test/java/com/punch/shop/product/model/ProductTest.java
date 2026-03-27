package com.punch.shop.product.model;

import com.punch.shop.product.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("상품A")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(5)
                .category(Category.builder().name("가전/디지털").build())
                .build();
    }

    @Test
    @DisplayName("재고 차감 성공")
    void reduceStockSuccess() {
        product.reduceStock(3);

        assertThat(product.getStockQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 부족 시 OutOfStockException 발생")
    void reduceStockOutOfStock() {
        assertThatThrownBy(() -> product.reduceStock(10))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("상품A");
    }

    @Test
    @DisplayName("재고와 동일한 수량 차감 성공")
    void reduceStockExactAmount() {
        product.reduceStock(5);

        assertThat(product.getStockQuantity()).isEqualTo(0);
    }
}
