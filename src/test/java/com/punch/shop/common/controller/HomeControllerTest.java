package com.punch.shop.common.controller;

import com.punch.shop.common.config.SecurityConfig;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.CustomUserDetailsService;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.service.ProductService;
import com.punch.shop.recommendation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "홍길동", "encodedPassword", List.of());
    }

    @Test
    @DisplayName("홈 페이지 요청 - 로그인 사용자는 최근 본 상품과 추천 상품을 5개씩 조회")
    void homeForAuthenticatedMember() throws Exception {
        List<Category> categories = List.of(Category.builder().name("가전/디지털").build());
        List<ProductResponse> products = List.of(
                ProductResponse.builder()
                        .id(1L)
                        .name("테스트 상품")
                        .price(BigDecimal.valueOf(10000))
                        .categoryName("가전/디지털")
                        .build()
        );

        given(productService.getCategories()).willReturn(categories);
        given(recommendationService.getRecentViewedProducts(1L, 5)).willReturn(products);
        given(recommendationService.getRecommendedProducts(1L, 5)).willReturn(products);

        mockMvc.perform(get("/").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("categories", "recentViewedProducts", "recommendedProducts"));

        verify(recommendationService).getRecentViewedProducts(1L, 5);
        verify(recommendationService).getRecommendedProducts(1L, 5);
    }

    @Test
    @DisplayName("홈 페이지 요청 - 비로그인 사용자는 추천 상품만 5개 조회")
    void homeForAnonymous() throws Exception {
        given(productService.getCategories()).willReturn(List.of());
        given(recommendationService.getRecentViewedProducts(null, 5)).willReturn(List.of());
        given(recommendationService.getRecommendedProducts(null, 5)).willReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("categories", "recentViewedProducts", "recommendedProducts"));

        verify(recommendationService).getRecentViewedProducts(null, 5);
        verify(recommendationService).getRecommendedProducts(null, 5);
    }
}
