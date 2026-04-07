package com.punch.shop.recommendation.model;

import com.punch.shop.member.model.Member;
import com.punch.shop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "browsing_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BrowsingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "browsing_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    public static BrowsingHistory create(Member member, Product product) {
        return BrowsingHistory.builder()
                .member(member)
                .product(product)
                .viewedAt(LocalDateTime.now())
                .build();
    }

    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }
}
