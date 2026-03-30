package com.punch.shop.product.controller;

import com.punch.shop.product.dto.ProductCreateRequest;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 폼 요청")
    void registerForm() throws Exception {
        given(productService.getCategories()).willReturn(List.of(
                Category.builder().name("가전/디지털").build()
        ));

        mockMvc.perform(get("/admin/products/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/register"))
                .andExpect(model().attributeExists("productCreateRequest", "categories"));
    }

    @Test
    @DisplayName("상품 등록 성공 - 상품 상세 페이지로 redirect")
    void registerSuccess() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "data".getBytes());

        given(productService.createProduct(any(ProductCreateRequest.class), any(MultipartFile.class))).willReturn(1L);

        mockMvc.perform(multipart("/admin/products")
                        .file(image)
                        .param("name", "테스트 상품")
                        .param("price", "10000")
                        .param("stockQuantity", "10")
                        .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/1"));
    }

    @Test
    @DisplayName("상품 등록 실패 - 유효성 검증 오류")
    void registerValidationFail() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "data".getBytes());
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(multipart("/admin/products")
                        .file(image)
                        .param("name", "")
                        .param("price", "10000")
                        .param("stockQuantity", "10")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/register"));
    }

    @Test
    @DisplayName("상품 등록 실패 - 이미지 미업로드")
    void registerWithoutImage() throws Exception {
        MockMultipartFile emptyImage = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
        given(productService.getCategories()).willReturn(List.of());

        mockMvc.perform(multipart("/admin/products")
                        .file(emptyImage)
                        .param("name", "테스트 상품")
                        .param("price", "10000")
                        .param("stockQuantity", "10")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/register"));
    }
}
