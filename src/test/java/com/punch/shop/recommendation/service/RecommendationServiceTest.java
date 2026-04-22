package com.punch.shop.recommendation.service;

import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.repository.ProductRepository;
import com.punch.shop.recommendation.model.BrowsingHistory;
import com.punch.shop.recommendation.repository.BrowsingHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Member member;
    private Category electronics;
    private Product viewedProduct;
    private Product relatedProduct;

    @BeforeEach
    void setUp() {
        member = Member.create("test@example.com", "pw", "홍길동", "010-1234-5678");
        ReflectionTestUtils.setField(member, "id", 1L);

        electronics = Category.builder().name("가전/디지털").build();
        ReflectionTestUtils.setField(electronics, "id", 10L);

        viewedProduct = Product.builder()
                .name("노트북")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(5)
                .category(electronics)
                .build();
        ReflectionTestUtils.setField(viewedProduct, "id", 100L);

        relatedProduct = Product.builder()
                .name("모니터")
                .price(BigDecimal.valueOf(500000))
                .stockQuantity(3)
                .category(electronics)
                .build();
        ReflectionTestUtils.setField(relatedProduct, "id", 101L);
    }

    @Test
    @DisplayName("브라우징 기록 - 처음 본 상품이면 새로 저장")
    void recordViewCreatesNewHistory() {
        given(browsingHistoryRepository.findByMemberIdAndProductId(1L, 100L))
                .willReturn(Optional.empty());
        given(memberRepository.getReferenceById(1L)).willReturn(member);
        given(productRepository.getReferenceById(100L)).willReturn(viewedProduct);

        recommendationService.recordView(1L, 100L);

        ArgumentCaptor<BrowsingHistory> captor = ArgumentCaptor.forClass(BrowsingHistory.class);
        verify(browsingHistoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getMember()).isEqualTo(member);
        assertThat(captor.getValue().getProduct()).isEqualTo(viewedProduct);
    }

    @Test
    @DisplayName("브라우징 기록 - 이미 본 상품이면 viewedAt만 갱신")
    void recordViewUpdatesExistingHistory() {
        BrowsingHistory existing = BrowsingHistory.create(member, viewedProduct);
        ReflectionTestUtils.setField(existing, "viewedAt", LocalDateTime.now().minusDays(1));

        given(browsingHistoryRepository.findByMemberIdAndProductId(1L, 100L))
                .willReturn(Optional.of(existing));

        LocalDateTime before = existing.getViewedAt();
        recommendationService.recordView(1L, 100L);

        assertThat(existing.getViewedAt()).isAfter(before);
        verify(browsingHistoryRepository, never()).save(any(BrowsingHistory.class));
    }

    @Test
    @DisplayName("추천 상품 - 비로그인 사용자는 인기 상품 반환")
    void getRecommendedProductsForAnonymous() {
        given(productRepository.findPopularProducts(any(Pageable.class)))
                .willReturn(List.of(viewedProduct));

        List<ProductResponse> result = recommendationService.getRecommendedProducts(null, 8);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        verify(browsingHistoryRepository, never()).findRecentCategoryIdsByMemberId(any());
    }

    @Test
    @DisplayName("추천 상품 - 브라우징 이력 없으면 인기 상품 폴백")
    void getRecommendedProductsFallbackWhenNoHistory() {
        given(browsingHistoryRepository.findRecentCategoryIdsByMemberId(1L))
                .willReturn(List.of());
        given(productRepository.findPopularProducts(any(Pageable.class)))
                .willReturn(List.of(viewedProduct));

        List<ProductResponse> result = recommendationService.getRecommendedProducts(1L, 8);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("추천 상품 - 최근 본 카테고리 기반으로 조회, 본 상품은 제외")
    void getRecommendedProductsByCategory() {
        given(browsingHistoryRepository.findRecentCategoryIdsByMemberId(1L))
                .willReturn(List.of(10L));
        given(browsingHistoryRepository.findRecentViewedProductIdsByMemberId(eq(1L), any(Pageable.class)))
                .willReturn(List.of(100L));
        given(productRepository.findRecommendedByCategories(eq(List.of(10L)), eq(List.of(100L)), any(Pageable.class)))
                .willReturn(List.of(relatedProduct));

        List<ProductResponse> result = recommendationService.getRecommendedProducts(1L, 8);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(101L);
        verify(productRepository, never()).findPopularProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("추천 상품 - 제외 대상이 비어있어도 쿼리 실패하지 않도록 더미 ID 사용")
    void getRecommendedProductsWithEmptyExcludeList() {
        given(browsingHistoryRepository.findRecentCategoryIdsByMemberId(1L))
                .willReturn(List.of(10L));
        given(browsingHistoryRepository.findRecentViewedProductIdsByMemberId(eq(1L), any(Pageable.class)))
                .willReturn(List.of());
        given(productRepository.findRecommendedByCategories(anyList(), eq(List.of(0L)), any(Pageable.class)))
                .willReturn(List.of(relatedProduct));

        List<ProductResponse> result = recommendationService.getRecommendedProducts(1L, 8);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("추천 상품 - 카테고리 기반 결과가 비어있으면 인기 상품 폴백")
    void getRecommendedProductsFallbackWhenCategoryResultEmpty() {
        given(browsingHistoryRepository.findRecentCategoryIdsByMemberId(1L))
                .willReturn(List.of(10L));
        given(browsingHistoryRepository.findRecentViewedProductIdsByMemberId(eq(1L), any(Pageable.class)))
                .willReturn(List.of(100L));
        given(productRepository.findRecommendedByCategories(anyList(), anyList(), any(Pageable.class)))
                .willReturn(List.of());
        given(productRepository.findPopularProducts(any(Pageable.class)))
                .willReturn(List.of(viewedProduct));

        List<ProductResponse> result = recommendationService.getRecommendedProducts(1L, 8);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("관련 상품 - 같은 카테고리에서 현재 상품 제외하고 조회")
    void getRelatedProducts() {
        given(productRepository.findRelatedProducts(eq(10L), eq(100L), any(Pageable.class)))
                .willReturn(List.of(relatedProduct));

        List<ProductResponse> result = recommendationService.getRelatedProducts(100L, 10L, 4);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(101L);
    }
}
