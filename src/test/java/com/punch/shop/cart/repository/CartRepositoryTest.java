package com.punch.shop.cart.repository;

import com.punch.shop.cart.model.Cart;
import com.punch.shop.common.annotation.RepositoryTest;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678"));

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

    @Test
    @DisplayName("회원 ID로 장바구니 조회 - EntityGraph로 상품 목록과 소계 함께 로딩")
    void findByMemberId_withItems() {
        Cart cart = Cart.create(member);
        cart.addItem(product, 2);
        cartRepository.save(cart);
        flushAndClear();

        Cart found = cartRepository.findByMemberId(member.getId()).orElseThrow();

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getProduct().getName()).isEqualTo("테스트 상품");
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(found.getItems().get(0).getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }
}
