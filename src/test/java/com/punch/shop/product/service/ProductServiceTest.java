package com.punch.shop.product.service;

import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().name("가전/디지털").build();
        ReflectionTestUtils.setField(category, "id", 1L);

        product = Product.builder()
                .name("테스트 상품")
                .description("상품 설명")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(10)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    void searchProductsSuccess() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        given(productRepository.search(eq(null), eq(null), any(Pageable.class))).willReturn(page);

        Page<ProductResponse> result = productService.searchProducts(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 상품");
        assertThat(result.getContent().get(0).getPrice()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("가전/디지털");
    }

    @Test
    @DisplayName("키워드 검색 성공")
    void searchProductsByKeyword() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        given(productRepository.search(eq("테스트"), eq(null), any(Pageable.class))).willReturn(page);

        Page<ProductResponse> result = productService.searchProducts("테스트", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("카테고리 필터링 성공")
    void searchProductsByCategoryId() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        given(productRepository.search(eq(null), eq(1L), any(Pageable.class))).willReturn(page);

        Page<ProductResponse> result = productService.searchProducts(null, 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("가전/디지털");
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductSuccess() {
        given(productRepository.findByIdAndStatus(1L, ProductStatus.ACTIVE)).willReturn(Optional.of(product));

        ProductDetailResponse result = productService.getProduct(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        assertThat(result.getDescription()).isEqualTo("상품 설명");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(result.getStockQuantity()).isEqualTo(10);
        assertThat(result.getCategoryName()).isEqualTo("가전/디지털");
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품이면 ProductNotFoundException 발생")
    void getProductNotFound() {
        given(productRepository.findByIdAndStatus(99L, ProductStatus.ACTIVE)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategoriesSuccess() {
        given(categoryRepository.findAll()).willReturn(List.of(category));

        List<Category> result = productService.getCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("가전/디지털");
    }
}
