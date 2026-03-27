package com.punch.shop.product.controller;

import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ProductResponse createProductResponse(Long id, String name, BigDecimal price) {
        return ProductResponse.builder()
                .id(id)
                .name(name)
                .price(price)
                .categoryName("가전/디지털")
                .build();
    }

    @Test
    @DisplayName("상품 목록 페이지 요청")
    void list() throws Exception {
        given(productService.searchProducts(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(
                        createProductResponse(1L, "상품A", BigDecimal.valueOf(10000)),
                        createProductResponse(2L, "상품B", BigDecimal.valueOf(20000))
                )));
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products", "sort", "categories"));
    }

    @Test
    @DisplayName("상품 목록 - 정렬 파라미터 전달")
    void listWithSort() throws Exception {
        given(productService.searchProducts(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/products").param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attribute("sort", "price_asc"));
    }

    @Test
    @DisplayName("상품 목록 - 키워드 검색")
    void listWithKeyword() throws Exception {
        given(productService.searchProducts(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(
                        createProductResponse(1L, "삼성 TV", BigDecimal.valueOf(500000))
                )));
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/products").param("keyword", "삼성"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attribute("keyword", "삼성"));
    }

    @Test
    @DisplayName("상품 목록 - 카테고리 필터링")
    void listWithCategoryId() throws Exception {
        given(productService.searchProducts(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(
                        createProductResponse(1L, "상품A", BigDecimal.valueOf(10000))
                )));
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(get("/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attribute("categoryId", 1L));
    }

    @Test
    @DisplayName("상품 상세 페이지 요청")
    void detail() throws Exception {
        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(1L)
                .name("테스트 상품")
                .description("상품 설명")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .categoryId(1L)
                .categoryName("가전/디지털")
                .status(ProductStatus.ACTIVE)
                .build();

        given(productService.getProduct(1L)).willReturn(response);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @DisplayName("상품 상세 페이지 요청 - 존재하지 않는 상품이면 404")
    void detailNotFound() throws Exception {
        given(productService.getProduct(99L)).willThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }
}
