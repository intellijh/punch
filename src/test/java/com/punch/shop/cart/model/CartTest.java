package com.punch.shop.cart.model;

import com.punch.shop.cart.exception.CartItemNotFoundException;
import com.punch.shop.member.model.Member;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private Cart cart;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        Member member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
        cart = Cart.create(member);

        Category category = Category.builder().name("가전/디지털").build();

        productA = Product.builder()
                .name("상품A")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .category(category)
                .build();
        ReflectionTestUtils.setField(productA, "id", 1L);

        productB = Product.builder()
                .name("상품B")
                .price(BigDecimal.valueOf(20000))
                .stockQuantity(5)
                .category(category)
                .build();
        ReflectionTestUtils.setField(productB, "id", 2L);
    }

    @Test
    @DisplayName("상품 추가 - 새 상품이 목록에 추가됨")
    void addItem_newProduct() {
        cart.addItem(productA, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct()).isEqualTo(productA);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("상품 추가 - 이미 담긴 상품은 수량 합산")
    void addItem_existingProduct_addsQuantity() {
        cart.addItem(productA, 2);

        cart.addItem(productA, 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 추가 - 다른 상품은 별도 항목으로 추가됨")
    void addItem_differentProducts() {
        cart.addItem(productA, 1);
        cart.addItem(productB, 2);

        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("수량 변경 성공")
    void updateItemQuantity_success() {
        cart.addItem(productA, 1);

        cart.updateItemQuantity(productA.getId(), 5);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 추가 실패 - 수량이 0 이하이면 IllegalArgumentException 발생")
    void addItem_invalidQuantity() {
        assertThatThrownBy(() -> cart.addItem(productA, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 이상");
    }

    @Test
    @DisplayName("수량 변경 실패 - 수량이 0 이하이면 IllegalArgumentException 발생")
    void updateItemQuantity_invalidQuantity() {
        cart.addItem(productA, 1);

        assertThatThrownBy(() -> cart.updateItemQuantity(productA.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 이상");
    }

    @Test
    @DisplayName("수량 변경 실패 - 없는 상품이면 CartItemNotFoundException 발생")
    void updateItemQuantity_notFound() {
        assertThatThrownBy(() -> cart.updateItemQuantity(99L, 5))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void removeItem_success() {
        cart.addItem(productA, 1);
        cart.addItem(productB, 2);

        cart.removeItem(productA.getId());

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct()).isEqualTo(productB);
    }

    @Test
    @DisplayName("없는 상품 삭제 - 목록 변화 없음")
    void removeItem_notFound() {
        cart.addItem(productA, 1);

        cart.removeItem(99L);

        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("총 금액 계산 - 모든 항목의 소계 합산")
    void getTotalPrice() {
        cart.addItem(productA, 2);  // 10000 * 2 = 20000
        cart.addItem(productB, 1);  // 20000 * 1 = 20000

        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(40000));
    }

    @Test
    @DisplayName("총 금액 계산 - 빈 장바구니는 0원")
    void getTotalPrice_empty() {
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
