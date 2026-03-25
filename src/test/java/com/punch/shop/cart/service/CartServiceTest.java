package com.punch.shop.cart.service;

import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.exception.CartNotFoundException;
import com.punch.shop.cart.model.Cart;
import com.punch.shop.cart.repository.CartRepository;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
        ReflectionTestUtils.setField(member, "id", 1L);

        Category category = Category.builder().name("가전/디지털").build();
        product = Product.builder()
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 존재")
    void getCart_exists() {
        Cart cart = Cart.create(member);
        cart.addItem(product, 2);
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));

        CartResponse result = cartService.getCart(member.getId());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 없으면 빈 응답 반환")
    void getCart_empty() {
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.empty());

        CartResponse result = cartService.getCart(member.getId());

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("상품 추가 성공 - 장바구니 없으면 새로 생성 후 저장")
    void addItem_newCart() {
        given(productRepository.findByIdAndStatus(product.getId(), ProductStatus.ACTIVE)).willReturn(Optional.of(product));
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.empty());
        given(memberRepository.getReferenceById(member.getId())).willReturn(member);
        given(cartRepository.save(any(Cart.class))).willReturn(Cart.create(member));

        cartService.addItem(member.getId(), product.getId(), 2);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("상품 추가 성공 - 기존 장바구니에 상품 추가")
    void addItem_existingCart() {
        Cart cart = Cart.create(member);
        given(productRepository.findByIdAndStatus(product.getId(), ProductStatus.ACTIVE)).willReturn(Optional.of(product));
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));

        cartService.addItem(member.getId(), product.getId(), 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("상품 추가 성공 - 이미 담긴 상품은 수량 합산")
    void addItem_existingProduct_addsQuantity() {
        Cart cart = Cart.create(member);
        cart.addItem(product, 1);
        given(productRepository.findByIdAndStatus(product.getId(), ProductStatus.ACTIVE)).willReturn(Optional.of(product));
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));

        cartService.addItem(member.getId(), product.getId(), 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("상품 추가 실패 - 존재하지 않는 상품이면 ProductNotFoundException 발생")
    void addItem_productNotFound() {
        given(productRepository.findByIdAndStatus(99L, ProductStatus.ACTIVE)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(member.getId(), 99L, 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("수량 변경 성공")
    void updateQuantity_success() {
        Cart cart = Cart.create(member);
        cart.addItem(product, 1);
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));

        cartService.updateQuantity(member.getId(), product.getId(), 5);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("수량 변경 실패 - 장바구니 없으면 CartNotFoundException 발생")
    void updateQuantity_cartNotFound() {
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(member.getId(), product.getId(), 5))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void removeItem_success() {
        Cart cart = Cart.create(member);
        cart.addItem(product, 1);
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));

        cartService.removeItem(member.getId(), product.getId());

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 장바구니 없으면 CartNotFoundException 발생")
    void removeItem_cartNotFound() {
        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(member.getId(), product.getId()))
                .isInstanceOf(CartNotFoundException.class);
    }
}
