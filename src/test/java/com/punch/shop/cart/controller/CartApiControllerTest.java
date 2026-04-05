package com.punch.shop.cart.controller;

import com.punch.shop.cart.dto.CartItemResponse;
import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.exception.CartNotFoundException;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartApiController.class)
class CartApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "홍길동", "encodedPassword", List.of());
    }

    @Test
    @DisplayName("수량 변경 성공")
    void updateQuantity() throws Exception {
        CartResponse updatedCart = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(1L).productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(3)
                        .subtotal(BigDecimal.valueOf(30000))
                        .checked(true).stockQuantity(10).outOfStock(false)
                        .build()))
                .totalPrice(BigDecimal.valueOf(30000))
                .build();

        willDoNothing().given(cartService).updateQuantity(1L, 1L, 3);
        given(cartService.getCart(1L)).willReturn(updatedCart);

        mockMvc.perform(patch("/api/cart/items/1/quantity")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(30000));
    }

    @Test
    @DisplayName("수량 변경 실패 - 장바구니 없으면 404")
    void updateQuantity_cartNotFound() throws Exception {
        willThrow(new CartNotFoundException()).given(cartService).updateQuantity(1L, 1L, 3);

        mockMvc.perform(patch("/api/cart/items/1/quantity")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 3}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상품 체크 해제 성공 - 합계에서 제외")
    void updateItemChecked() throws Exception {
        CartResponse updatedCart = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(1L).productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(2)
                        .subtotal(BigDecimal.valueOf(20000))
                        .checked(false).stockQuantity(10).outOfStock(false)
                        .build()))
                .totalPrice(BigDecimal.ZERO)
                .build();

        willDoNothing().given(cartService).updateItemChecked(1L, 1L, false);
        given(cartService.getCart(1L)).willReturn(updatedCart);

        mockMvc.perform(patch("/api/cart/items/1/checked")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].checked").value(false))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @DisplayName("전체 선택/해제 성공")
    void checkAllItems() throws Exception {
        CartResponse updatedCart = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(1L).productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(2)
                        .subtotal(BigDecimal.valueOf(20000))
                        .checked(true).stockQuantity(10).outOfStock(false)
                        .build()))
                .totalPrice(BigDecimal.valueOf(20000))
                .build();

        willDoNothing().given(cartService).checkAllItems(1L, true);
        given(cartService.getCart(1L)).willReturn(updatedCart);

        mockMvc.perform(patch("/api/cart/check-all")
                        .with(user(principal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].checked").value(true))
                .andExpect(jsonPath("$.totalPrice").value(20000));
    }

    @Test
    @DisplayName("장바구니 상품 수 조회")
    void getCartItemCount() throws Exception {
        given(cartService.getCartItemCount(1L)).willReturn(3);

        mockMvc.perform(get("/api/cart/count")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
