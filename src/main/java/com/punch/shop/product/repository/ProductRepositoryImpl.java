package com.punch.shop.product.repository;

import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.punch.shop.product.model.QCategory.category;
import static com.punch.shop.product.model.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(String keyword, Long categoryId, Pageable pageable) {
        BooleanBuilder where = buildWhere(keyword, categoryId);

        List<Product> content = queryFactory
                .selectFrom(product)
                .join(product.category, category).fetchJoin()
                .where(where)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(product.count())
                .from(product)
                .join(product.category, category)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanBuilder buildWhere(String keyword, Long categoryId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(product.status.eq(ProductStatus.ACTIVE));
        if (keyword != null && !keyword.isBlank()) {
            builder.and(product.name.containsIgnoreCase(keyword));
        }
        if (categoryId != null) {
            builder.and(category.id.eq(categoryId));
        }
        return builder;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(order -> switch (order.getProperty()) {
                    case "price" -> order.isAscending() ? product.price.asc() : product.price.desc();
                    case "orderCount" -> product.orderCount.desc();
                    default -> order.isAscending() ? product.createdAt.asc() : product.createdAt.desc();
                })
                .toArray(OrderSpecifier[]::new);
    }
}
