package com.punch.shop.cart.controller;

import com.punch.shop.cart.dto.CartItemResponse;
import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.exception.CartNotFoundException;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.CustomUserDetailsService;
import com.punch.shop.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "encodedPassword", List.of());
        cartResponse = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(1L).productId(1L).productName("테스트 상품")
                        .price(BigDecimal.valueOf(10000)).quantity(2)
                        .subtotal(BigDecimal.valueOf(20000))
                        .build()))
                .totalPrice(BigDecimal.valueOf(20000))
                .build();
    }

    @Test
    @DisplayName("장바구니 페이지 요청")
    void cart() throws Exception {
        given(cartService.getCart(1L)).willReturn(cartResponse);

        mockMvc.perform(get("/cart").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart"))
                .andExpect(model().attributeExists("cart"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 성공")
    void addItem() throws Exception {
        willDoNothing().given(cartService).addItem(1L, 1L, 2);

        mockMvc.perform(post("/cart/items").with(user(principal)).with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 존재하지 않는 상품이면 404")
    void addItem_productNotFound() throws Exception {
        willThrow(new ProductNotFoundException(99L)).given(cartService).addItem(1L, 99L, 1);

        mockMvc.perform(post("/cart/items").with(user(principal)).with(csrf())
                        .param("productId", "99")
                        .param("quantity", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 수량이 0 이하이면 400")
    void addItem_invalidQuantity() throws Exception {
        mockMvc.perform(post("/cart/items").with(user(principal)).with(csrf())
                        .param("productId", "1")
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수량 변경 성공")
    void updateQuantity() throws Exception {
        willDoNothing().given(cartService).updateQuantity(1L, 1L, 5);

        mockMvc.perform(post("/cart/items/1").with(user(principal)).with(csrf())
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @DisplayName("수량 변경 실패 - 수량이 0 이하이면 400")
    void updateQuantity_invalidQuantity() throws Exception {
        mockMvc.perform(post("/cart/items/1").with(user(principal)).with(csrf())
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수량 변경 실패 - 장바구니 없으면 404")
    void updateQuantity_cartNotFound() throws Exception {
        willThrow(new CartNotFoundException()).given(cartService).updateQuantity(1L, 1L, 5);

        mockMvc.perform(post("/cart/items/1").with(user(principal)).with(csrf())
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void removeItem() throws Exception {
        willDoNothing().given(cartService).removeItem(1L, 1L);

        mockMvc.perform(post("/cart/items/1/delete").with(user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @DisplayName("상품 삭제 실패 - 장바구니 없으면 404")
    void removeItem_cartNotFound() throws Exception {
        willThrow(new CartNotFoundException()).given(cartService).removeItem(1L, 1L);

        mockMvc.perform(post("/cart/items/1/delete").with(user(principal)).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
