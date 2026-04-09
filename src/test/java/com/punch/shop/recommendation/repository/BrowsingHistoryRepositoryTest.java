package com.punch.shop.recommendation.repository;

import com.punch.shop.common.annotation.RepositoryTest;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
import com.punch.shop.recommendation.model.BrowsingHistory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class BrowsingHistoryRepositoryTest {

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    private Category electronics;
    private Category fashion;
    private Product productA;
    private Product productB;
    private Product productC;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                Member.create("test@example.com", "encodedPassword", "홍길동", "010-1234-5678"));

        electronics = categoryRepository.save(Category.builder().name("가전/디지털").build());
        fashion = categoryRepository.save(Category.builder().name("패션/의류").build());

        productA = productRepository.save(Product.builder()
                .name("노트북")
                .price(BigDecimal.valueOf(1000000))
                .stockQuantity(10)
                .category(electronics)
                .status(ProductStatus.ACTIVE)
                .build());

        productB = productRepository.save(Product.builder()
                .name("셔츠")
                .price(BigDecimal.valueOf(30000))
                .stockQuantity(20)
                .category(fashion)
                .status(ProductStatus.ACTIVE)
                .build());

        productC = productRepository.save(Product.builder()
                .name("모니터")
                .price(BigDecimal.valueOf(500000))
                .stockQuantity(5)
                .category(electronics)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("브라우징 히스토리 저장 - ID 및 viewedAt 설정 확인")
    void save() {
        BrowsingHistory saved = browsingHistoryRepository.save(
                BrowsingHistory.create(member, productA));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMember().getId()).isEqualTo(member.getId());
        assertThat(saved.getProduct().getId()).isEqualTo(productA.getId());
        assertThat(saved.getViewedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 ID와 상품 ID로 브라우징 히스토리 조회")
    void findByMemberIdAndProductId() {
        browsingHistoryRepository.save(BrowsingHistory.create(member, productA));
        flushAndClear();

        Optional<BrowsingHistory> found = browsingHistoryRepository
                .findByMemberIdAndProductId(member.getId(), productA.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getProduct().getId()).isEqualTo(productA.getId());
    }

    @Test
    @DisplayName("존재하지 않는 브라우징 히스토리 조회 시 빈 Optional 반환")
    void findByMemberIdAndProductId_notFound() {
        Optional<BrowsingHistory> found = browsingHistoryRepository
                .findByMemberIdAndProductId(member.getId(), productA.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("회원의 최근 브라우징 카테고리 ID 목록 조회 - 중복 제거")
    void findRecentCategoryIdsByMemberId() {
        browsingHistoryRepository.save(BrowsingHistory.create(member, productA));
        browsingHistoryRepository.save(BrowsingHistory.create(member, productB));
        browsingHistoryRepository.save(BrowsingHistory.create(member, productC));
        flushAndClear();

        List<Long> categoryIds = browsingHistoryRepository
                .findRecentCategoryIdsByMemberId(member.getId());

        assertThat(categoryIds).hasSize(2);
        assertThat(categoryIds).containsExactlyInAnyOrder(electronics.getId(), fashion.getId());
    }

    @Test
    @DisplayName("회원이 최근 본 상품 ID 목록 조회 - Pageable로 개수 제한")
    void findRecentViewedProductIdsByMemberId() {
        browsingHistoryRepository.save(BrowsingHistory.create(member, productA));
        browsingHistoryRepository.save(BrowsingHistory.create(member, productB));
        browsingHistoryRepository.save(BrowsingHistory.create(member, productC));
        flushAndClear();

        List<Long> productIds = browsingHistoryRepository
                .findRecentViewedProductIdsByMemberId(member.getId(), PageRequest.of(0, 2));

        assertThat(productIds).hasSize(2);
    }

    @Test
    @DisplayName("브라우징 히스토리가 없는 회원은 빈 리스트 반환")
    void findRecentViewedProductIdsByMemberId_empty() {
        List<Long> productIds = browsingHistoryRepository
                .findRecentViewedProductIdsByMemberId(member.getId(), PageRequest.of(0, 10));

        assertThat(productIds).isEmpty();
    }
}
