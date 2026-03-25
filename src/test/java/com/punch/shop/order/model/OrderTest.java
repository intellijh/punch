package com.punch.shop.order.model;

import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private Member member;
    private Address address;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");

        address = Address.create(member, "홍길동", "010-1234-5678", "12345",
                "서울시 강남구", null, "집", true);

        Category category = Category.builder().name("가전/디지털").build();

        productA = Product.builder()
                .name("상품A")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(5)
                .category(category)
                .build();
        ReflectionTestUtils.setField(productA, "id", 1L);

        productB = Product.builder()
                .name("상품B")
                .price(BigDecimal.valueOf(20000))
                .stockQuantity(3)
                .category(category)
                .build();
        ReflectionTestUtils.setField(productB, "id", 2L);
    }

    @Test
    @DisplayName("주문 생성 - 배송지 정보 스냅샷 저장")
    void createOrder() {
        Order order = Order.create(member, address, PaymentMethod.CREDIT_CARD);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(order.getRecipientName()).isEqualTo("홍길동");
        assertThat(order.getZipCode()).isEqualTo("12345");
        assertThat(order.getAddress()).isEqualTo("서울시 강남구");
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    @DisplayName("아이템 추가 및 총액 계산")
    void addItemAndCalculateTotalPrice() {
        Order order = Order.create(member, address, PaymentMethod.KAKAO_PAY);

        order.addItem(productA, 2);
        order.addItem(productB, 1);

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(40000));
    }

    @Test
    @DisplayName("OrderItem - 주문 당시 상품명/가격 스냅샷 저장")
    void orderItemSnapshot() {
        Order order = Order.create(member, address, PaymentMethod.NAVER_PAY);
        order.addItem(productA, 3);

        OrderItem item = order.getItems().get(0);

        assertThat(item.getProductName()).isEqualTo("상품A");
        assertThat(item.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("주문 상태 변경")
    void updateStatus() {
        Order order = Order.create(member, address, PaymentMethod.TOSS_PAY);

        order.updateStatus(OrderStatus.SHIPPING);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }
}
