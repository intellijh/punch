package com.punch.shop.order.service;

import com.punch.shop.cart.exception.EmptyCartException;
import com.punch.shop.cart.model.Cart;
import com.punch.shop.cart.repository.CartRepository;
import com.punch.shop.member.exception.AddressNotFoundException;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.AddressRepository;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.order.dto.OrderCreateRequest;
import com.punch.shop.order.dto.OrderListItemResponse;
import com.punch.shop.order.dto.OrderResponse;
import com.punch.shop.order.exception.OrderNotFoundException;
import com.punch.shop.order.model.Order;
import com.punch.shop.order.model.OrderStatus;
import com.punch.shop.order.model.PaymentMethod;
import com.punch.shop.order.repository.OrderRepository;
import com.punch.shop.product.exception.OutOfStockException;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;

    private Member member;
    private Address address;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678");
        ReflectionTestUtils.setField(member, "id", 1L);

        address = Address.create(member, "홍길동", "010-1234-5678", "12345",
                "서울시 강남구", null, "집", true);
        ReflectionTestUtils.setField(address, "id", 10L);

        Category category = Category.builder().name("가전/디지털").build();
        product = Product.builder()
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", 100L);

        cart = Cart.create(member);
        cart.addItem(product, 2);
    }

    @Test
    @DisplayName("주문 생성 성공 - 재고 차감 및 장바구니 비우기")
    void createOrderSuccess() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .addressId(address.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));
        given(addressRepository.findByIdAndMemberId(address.getId(), member.getId())).willReturn(Optional.of(address));
        given(memberRepository.getReferenceById(member.getId())).willReturn(member);
        given(orderRepository.save(any(Order.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(member.getId(), request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(response.getItems()).hasSize(1);
        assertThat(product.getStockQuantity()).isEqualTo(8);
        assertThat(cart.getItems()).isEmpty();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 빈 장바구니")
    void createOrderEmptyCart() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .addressId(address.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 배송지")
    void createOrderAddressNotFound() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .addressId(address.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cart));
        given(addressRepository.findByIdAndMemberId(address.getId(), member.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    @DisplayName("주문 생성 실패 - 재고 부족")
    void createOrderOutOfStock() {
        Product lowStockProduct = Product.builder()
                .name("품절 상품")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(1)
                .category(Category.builder().name("가전/디지털").build())
                .build();
        ReflectionTestUtils.setField(lowStockProduct, "id", 200L);

        Cart cartWithLowStock = Cart.create(member);
        cartWithLowStock.addItem(lowStockProduct, 5);

        OrderCreateRequest request = OrderCreateRequest.builder()
                .addressId(address.getId())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        given(cartRepository.findByMemberId(member.getId())).willReturn(Optional.of(cartWithLowStock));
        given(addressRepository.findByIdAndMemberId(address.getId(), member.getId())).willReturn(Optional.of(address));
        given(memberRepository.getReferenceById(member.getId())).willReturn(member);

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(OutOfStockException.class);
    }

    @Test
    @DisplayName("주문 목록 페이지 조회")
    void getOrders() {
        Order order = Order.create(member, address, PaymentMethod.CREDIT_CARD);
        order.addItem(product, 2);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order));

        given(orderRepository.findByMemberId(member.getId(), pageable)).willReturn(orderPage);

        Page<OrderListItemResponse> result = orderService.getOrders(member.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstItemName()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderSuccess() {
        Order order = Order.create(member, address, PaymentMethod.KAKAO_PAY);
        order.addItem(product, 2);
        ReflectionTestUtils.setField(order, "id", 1000L);

        given(orderRepository.findByIdAndMemberId(order.getId(), member.getId())).willReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(member.getId(), order.getId());

        assertThat(response.getOrderId()).isEqualTo(order.getId());
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.KAKAO_PAY);
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 존재하지 않는 주문")
    void getOrderNotFound() {
        Long nonExistentOrderId = 999L;
        given(orderRepository.findByIdAndMemberId(nonExistentOrderId, member.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(member.getId(), nonExistentOrderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
