package com.punch.shop.product.repository;

import com.punch.shop.common.annotation.RepositoryTest;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private Category category;
    private Category anotherCategory;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder().name("가전/디지털").build());
        anotherCategory = categoryRepository.save(Category.builder().name("의류").build());
    }

    private Product createProduct(String name, BigDecimal price, ProductStatus status) {
        return createProduct(name, price, status, category);
    }

    private Product createProduct(String name, BigDecimal price, ProductStatus status, Category cat) {
        return Product.builder()
                .name(name)
                .description("상품 설명")
                .price(price)
                .stockQuantity(10)
                .category(cat)
                .status(status)
                .build();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상품 저장 - ID 및 Auditing 적용 확인")
    void save() {
        Product product = createProduct("테스트 상품", BigDecimal.valueOf(10000), ProductStatus.ACTIVE);

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트 상품");
        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("전체 검색 - ACTIVE 상품만 반환")
    void search_activeOnly() {
        productRepository.save(createProduct("판매중 상품", BigDecimal.valueOf(10000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("판매중지 상품", BigDecimal.valueOf(20000), ProductStatus.INACTIVE));
        flushAndClear();

        Page<Product> result = productRepository.search(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("판매중 상품");
        assertThat(result.getContent().get(0).getCategory().getName()).isEqualTo("가전/디지털");
    }

    @Test
    @DisplayName("전체 검색 - 페이지네이션 첫 번째 페이지")
    void search_firstPage() {
        productRepository.save(createProduct("상품A", BigDecimal.valueOf(30000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("상품B", BigDecimal.valueOf(10000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("상품C", BigDecimal.valueOf(20000), ProductStatus.ACTIVE));
        flushAndClear();

        Page<Product> result = productRepository.search(
                null, null, PageRequest.of(0, 2, Sort.by("price").ascending()));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("상품B");
        assertThat(result.getContent().get(1).getName()).isEqualTo("상품C");
    }

    @Test
    @DisplayName("전체 검색 - 페이지네이션 두 번째 페이지")
    void search_secondPage() {
        productRepository.save(createProduct("상품A", BigDecimal.valueOf(30000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("상품B", BigDecimal.valueOf(10000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("상품C", BigDecimal.valueOf(20000), ProductStatus.ACTIVE));
        flushAndClear();

        Page<Product> result = productRepository.search(
                null, null, PageRequest.of(1, 2, Sort.by("price").ascending()));

        assertThat(result.isLast()).isTrue();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("상품A");
    }

    @Test
    @DisplayName("키워드 검색 - 상품명에 포함된 경우만 반환")
    void search_withKeyword() {
        productRepository.save(createProduct("삼성 TV", BigDecimal.valueOf(500000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("LG 냉장고", BigDecimal.valueOf(800000), ProductStatus.ACTIVE));
        productRepository.save(createProduct("삼성 세탁기", BigDecimal.valueOf(600000), ProductStatus.ACTIVE));
        flushAndClear();

        Page<Product> result = productRepository.search("삼성", null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("삼성 TV", "삼성 세탁기");
    }

    @Test
    @DisplayName("카테고리 필터링 - 해당 카테고리 상품만 반환")
    void search_withCategoryId() {
        productRepository.save(createProduct("가전 상품", BigDecimal.valueOf(100000), ProductStatus.ACTIVE, category));
        productRepository.save(createProduct("의류 상품", BigDecimal.valueOf(50000), ProductStatus.ACTIVE, anotherCategory));
        flushAndClear();

        Page<Product> result = productRepository.search(null, category.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("가전 상품");
    }

    @Test
    @DisplayName("ID와 상태로 상품 단건 조회 - 카테고리 함께 로딩 확인")
    void findByIdAndStatus() {
        Product saved = productRepository.save(
                createProduct("테스트 상품", BigDecimal.valueOf(10000), ProductStatus.ACTIVE));
        flushAndClear();

        Optional<Product> found = productRepository.findByIdAndStatus(saved.getId(), ProductStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 상품");
        assertThat(found.get().getCategory().getName()).isEqualTo("가전/디지털");
    }

    @Test
    @DisplayName("INACTIVE 상품은 ACTIVE 조회 시 반환되지 않음")
    void findByIdAndStatus_inactive() {
        Product saved = productRepository.save(
                createProduct("판매중지 상품", BigDecimal.valueOf(10000), ProductStatus.INACTIVE));
        flushAndClear();

        Optional<Product> found = productRepository.findByIdAndStatus(saved.getId(), ProductStatus.ACTIVE);

        assertThat(found).isEmpty();
    }
}
