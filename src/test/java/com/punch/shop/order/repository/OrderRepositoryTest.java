package com.punch.shop.order.repository;

import com.punch.shop.common.annotation.RepositoryTest;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.order.model.Order;
import com.punch.shop.order.model.OrderStatus;
import com.punch.shop.order.model.PaymentMethod;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    private Address address;
    private Product product;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678"));

        address = Address.create(member, "홍길동", "010-1234-5678", "12345",
                "서울시 강남구", null, "집", true);

        Category category = categoryRepository.save(
                Category.builder().name("가전/디지털").build());

        product = productRepository.save(Product.builder()
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Order createAndSaveOrder(PaymentMethod paymentMethod) {
        Order order = Order.create(member, address, paymentMethod);
        order.addItem(product, 2);
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("주문 저장 및 ID로 조회")
    void saveAndFindById() {
        Order saved = createAndSaveOrder(PaymentMethod.CREDIT_CARD);
        flushAndClear();

        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(found.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(found.getRecipientName()).isEqualTo("홍길동");
        assertThat(found.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("회원 ID로 주문 목록 페이지 조회")
    void findByMemberIdWithPaging() {
        createAndSaveOrder(PaymentMethod.CREDIT_CARD);
        createAndSaveOrder(PaymentMethod.KAKAO_PAY);
        createAndSaveOrder(PaymentMethod.NAVER_PAY);
        flushAndClear();

        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> page = orderRepository.findByMemberId(member.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("주문 ID와 회원 ID로 조회 성공")
    void findByIdAndMemberId() {
        Order saved = createAndSaveOrder(PaymentMethod.TOSS_PAY);
        flushAndClear();

        Optional<Order> found = orderRepository.findByIdAndMemberId(saved.getId(), member.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPaymentMethod()).isEqualTo(PaymentMethod.TOSS_PAY);
    }

    @Test
    @DisplayName("주문 ID와 회원 ID 불일치 시 조회 실패")
    void findByIdAndMemberIdNotFound() {
        Order saved = createAndSaveOrder(PaymentMethod.CREDIT_CARD);
        flushAndClear();

        Optional<Order> found = orderRepository.findByIdAndMemberId(saved.getId(), 999L);

        assertThat(found).isEmpty();
    }
}
