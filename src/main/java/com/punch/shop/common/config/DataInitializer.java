package com.punch.shop.common.config;

import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        log.info("샘플 데이터 초기화 시작");

        Category electronics = categoryRepository.save(Category.builder().name("가전/디지털").build());
        Category fashion = categoryRepository.save(Category.builder().name("패션/의류").build());
        Category food = categoryRepository.save(Category.builder().name("식품").build());
        Category beauty = categoryRepository.save(Category.builder().name("뷰티").build());
        Category sports = categoryRepository.save(Category.builder().name("스포츠/레저").build());

        productRepository.saveAll(List.of(
                Product.builder().name("삼성 갤럭시 S25 256GB").description("최신 AI 기능이 탑재된 플래그십 스마트폰입니다.").price(BigDecimal.valueOf(1190000)).stockQuantity(50).category(electronics).build(),
                Product.builder().name("LG 올레드 TV 55인치").description("완벽한 블랙과 선명한 색감을 자랑하는 올레드 TV입니다.").price(BigDecimal.valueOf(1590000)).stockQuantity(20).category(electronics).build(),
                Product.builder().name("애플 에어팟 프로 2세대").description("노이즈 캔슬링 기능이 강화된 무선 이어폰입니다.").price(BigDecimal.valueOf(359000)).stockQuantity(100).category(electronics).build(),
                Product.builder().name("다이슨 v15 무선청소기").description("레이저 먼지 감지 기술이 적용된 프리미엄 무선청소기입니다.").price(BigDecimal.valueOf(899000)).stockQuantity(30).category(electronics).build(),

                Product.builder().name("나이키 에어맥스 270").description("편안한 착용감과 스타일리시한 디자인의 운동화입니다.").price(BigDecimal.valueOf(149000)).stockQuantity(80).category(fashion).build(),
                Product.builder().name("아디다스 트레이닝 세트").description("흡습속건 소재의 기능성 트레이닝복 세트입니다.").price(BigDecimal.valueOf(89000)).stockQuantity(60).category(fashion).build(),
                Product.builder().name("유니클로 히트텍 극세사").description("따뜻하고 가벼운 겨울철 필수 이너웨어입니다.").price(BigDecimal.valueOf(29900)).stockQuantity(200).category(fashion).build(),

                Product.builder().name("곰곰 제주 삼다수 2L x 12개").description("깨끗한 제주 화산암반수입니다.").price(BigDecimal.valueOf(14900)).stockQuantity(500).category(food).build(),
                Product.builder().name("신라면 멀티팩 5개입").description("매콤한 국물이 일품인 인기 라면입니다.").price(BigDecimal.valueOf(4800)).stockQuantity(1000).category(food).build(),
                Product.builder().name("스타벅스 원두 파이크 플레이스 500g").description("부드럽고 균형잡힌 스타벅스 시그니처 원두입니다.").price(BigDecimal.valueOf(28000)).stockQuantity(150).category(food).build(),

                Product.builder().name("설화수 자음생 크림 60ml").description("한방 성분으로 피부 탄력을 높여주는 프리미엄 크림입니다.").price(BigDecimal.valueOf(95000)).stockQuantity(40).category(beauty).build(),
                Product.builder().name("아이오페 레티놀 엑스퍼트 0.1%").description("레티놀 성분의 안티에이징 세럼입니다.").price(BigDecimal.valueOf(52000)).stockQuantity(70).category(beauty).build(),

                Product.builder().name("요넥스 아스트록스 88S 배드민턴 라켓").description("공격형 플레이어를 위한 프리미엄 배드민턴 라켓입니다.").price(BigDecimal.valueOf(189000)).stockQuantity(25).category(sports).build(),
                Product.builder().name("레킷 요가매트 6mm").description("미끄럼 방지 기능의 고밀도 요가매트입니다.").price(BigDecimal.valueOf(35000)).stockQuantity(90).category(sports).build()
        ));

        log.info("샘플 데이터 초기화 완료: 카테고리 5개, 상품 14개");
    }
}
