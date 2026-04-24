package com.punch.shop.recommendation.service;

import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.repository.ProductRepository;
import com.punch.shop.recommendation.model.BrowsingHistory;
import com.punch.shop.recommendation.repository.BrowsingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int RECOMMENDATION_EXCLUDE_VIEWED_LIMIT = 5;

    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void recordView(Long memberId, Long productId) {
        browsingHistoryRepository.findByMemberIdAndProductId(memberId, productId)
                .ifPresentOrElse(
                        BrowsingHistory::updateViewedAt,
                        () -> {
                            Member member = memberRepository.getReferenceById(memberId);
                            Product product = productRepository.getReferenceById(productId);
                            browsingHistoryRepository.save(BrowsingHistory.create(member, product));
                        });
    }

    public List<ProductResponse> getRecommendedProducts(Long memberId, int limit) {
        if (memberId == null) {
            return popular(limit);
        }

        List<Long> categoryIds = browsingHistoryRepository.findRecentCategoryIdsByMemberId(memberId);
        if (categoryIds.isEmpty()) {
            return popular(limit);
        }

        List<Long> excluded = browsingHistoryRepository.findRecentViewedProductIdsByMemberId(
                memberId, PageRequest.of(0, RECOMMENDATION_EXCLUDE_VIEWED_LIMIT));
        if (excluded.isEmpty()) {
            excluded = List.of(0L);
        }

        List<Product> products = productRepository.findRecommendedByCategories(
                categoryIds, excluded, PageRequest.of(0, limit));
        if (products.isEmpty()) {
            return popular(limit);
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> getRecentViewedProducts(Long memberId, int limit) {
        if (memberId == null) {
            return List.of();
        }

        return browsingHistoryRepository.findRecentViewedProductsByMemberId(memberId, PageRequest.of(0, limit))
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> getRelatedProducts(Long productId, Long categoryId, int limit) {
        return productRepository.findRelatedProducts(categoryId, productId, PageRequest.of(0, limit))
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    private List<ProductResponse> popular(int limit) {
        return productRepository.findPopularProducts(PageRequest.of(0, limit))
                .stream()
                .map(ProductResponse::from)
                .toList();
    }
}
