package com.punch.shop.recommendation.repository;

import com.punch.shop.recommendation.model.BrowsingHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    Optional<BrowsingHistory> findByMemberIdAndProductId(Long memberId, Long productId);

    @Query("SELECT bh.product.category.id FROM BrowsingHistory bh " +
            "WHERE bh.member.id = :memberId " +
            "GROUP BY bh.product.category.id " +
            "ORDER BY MAX(bh.viewedAt) DESC")
    List<Long> findRecentCategoryIdsByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT bh.product.id FROM BrowsingHistory bh " +
            "WHERE bh.member.id = :memberId " +
            "ORDER BY bh.viewedAt DESC")
    List<Long> findRecentViewedProductIdsByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
