package com.punch.shop.order.controller;

import com.punch.shop.cart.dto.CartItemResponse;
import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.dto.AddressResponse;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.AddressService;
import com.punch.shop.member.service.CustomUserDetailsService;
import com.punch.shop.order.dto.OrderCreateRequest;
import com.punch.shop.order.dto.OrderItemResponse;
import com.punch.shop.order.dto.OrderListItemResponse;
import com.punch.shop.order.dto.OrderResponse;
import com.punch.shop.order.exception.OrderNotFoundException;
import com.punch.shop.order.model.OrderStatus;
import com.punch.shop.order.model.PaymentMethod;
import com.punch.shop.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;
    private CartResponse cartResponse;
    private AddressResponse addressResponse;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "홍길동", "encodedPassword", List.of());

        cartResponse = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(1L).productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(2)
                        .subtotal(BigDecimal.valueOf(20000))
                        .build()))
                .totalPrice(BigDecimal.valueOf(20000))
                .build();

        addressResponse = AddressResponse.builder()
                .id(1L).label("집").recipientName("홍길동")
                .phone("010-1234-5678").zipCode("12345")
                .address("서울시 강남구").addressDetail("101호")
                .defaultAddress(true)
                .build();

        orderResponse = OrderResponse.builder()
                .orderId(1L).status(OrderStatus.PAID).statusDescription("결제 완료")
                .paymentMethod(PaymentMethod.CREDIT_CARD).paymentMethodDescription("신용카드")
                .recipientName("홍길동").phone("010-1234-5678")
                .zipCode("12345").address("서울시 강남구").addressDetail("101호")
                .items(List.of(OrderItemResponse.builder()
                        .productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(2)
                        .subtotal(BigDecimal.valueOf(20000))
                        .build()))
                .totalPrice(BigDecimal.valueOf(20000))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("주문/결제 페이지 정상 조회")
    void checkoutForm() throws Exception {
        given(cartService.getCart(1L)).willReturn(cartResponse);
        given(addressService.getAddresses(1L)).willReturn(List.of(addressResponse));

        mockMvc.perform(get("/orders/checkout").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("order/checkout"))
                .andExpect(model().attributeExists("cart", "addresses", "selectedAddress",
                        "paymentMethods", "orderCreateRequest", "addressCreateRequest"));
    }

    @Test
    @DisplayName("주문/결제 페이지 - 기본 배송지가 selectedAddress로 설정됨")
    void checkoutForm_defaultAddressSelected() throws Exception {
        AddressResponse nonDefault = AddressResponse.builder()
                .id(2L).label("회사").recipientName("홍길동")
                .phone("010-0000-0000").zipCode("54321")
                .address("서울시 서초구").defaultAddress(false)
                .build();

        given(cartService.getCart(1L)).willReturn(cartResponse);
        given(addressService.getAddresses(1L)).willReturn(List.of(nonDefault, addressResponse));

        mockMvc.perform(get("/orders/checkout").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedAddress", addressResponse));
    }

    @Test
    @DisplayName("주문 생성 성공 - 주문 완료 페이지로 리다이렉트")
    void createOrder() throws Exception {
        given(orderService.createOrder(eq(1L), any(OrderCreateRequest.class))).willReturn(orderResponse);

        mockMvc.perform(post("/orders").with(user(principal)).with(csrf())
                        .param("addressId", "1")
                        .param("paymentMethod", "CREDIT_CARD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1/confirmation"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 배송지 미선택이면 주문/결제 페이지로 돌아옴")
    void createOrder_noAddress() throws Exception {
        given(cartService.getCart(1L)).willReturn(cartResponse);
        given(addressService.getAddresses(1L)).willReturn(List.of(addressResponse));

        mockMvc.perform(post("/orders").with(user(principal)).with(csrf())
                        .param("paymentMethod", "CREDIT_CARD"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/checkout"));
    }

    @Test
    @DisplayName("주문 생성 실패 - 결제수단 미선택이면 주문/결제 페이지로 돌아옴")
    void createOrder_noPaymentMethod() throws Exception {
        given(cartService.getCart(1L)).willReturn(cartResponse);
        given(addressService.getAddresses(1L)).willReturn(List.of(addressResponse));

        mockMvc.perform(post("/orders").with(user(principal)).with(csrf())
                        .param("addressId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/checkout"));
    }

    @Test
    @DisplayName("주문 완료 페이지 조회")
    void confirmation() throws Exception {
        given(orderService.getOrder(1L, 1L)).willReturn(orderResponse);

        mockMvc.perform(get("/orders/1/confirmation").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("order/detail"))
                .andExpect(model().attribute("confirmed", true))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("주문 상세 페이지 조회")
    void orderDetail() throws Exception {
        given(orderService.getOrder(1L, 1L)).willReturn(orderResponse);

        mockMvc.perform(get("/orders/1").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("order/detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 존재하지 않는 주문이면 404")
    void orderDetail_notFound() throws Exception {
        given(orderService.getOrder(1L, 99L)).willThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99").with(user(principal)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 목록 페이지 조회")
    void orderList() throws Exception {
        OrderListItemResponse listItem = OrderListItemResponse.builder()
                .orderId(1L).status(OrderStatus.PAID).statusDescription("결제 완료")
                .totalPrice(BigDecimal.valueOf(20000)).itemCount(1)
                .firstItemName("테스트 상품").createdAt(LocalDateTime.now())
                .build();

        given(orderService.getOrders(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(listItem), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/orders").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andExpect(model().attributeExists("orders"));
    }
}
