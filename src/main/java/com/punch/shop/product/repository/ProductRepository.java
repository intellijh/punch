package com.punch.shop.product.repository;

import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND p.category.id IN :categoryIds " +
            "AND p.id NOT IN :excludeProductIds " +
            "ORDER BY p.orderCount DESC")
    List<Product> findRecommendedByCategories(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludeProductIds") List<Long> excludeProductIds,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.status = 'ACTIVE' " +
            "ORDER BY p.orderCount DESC")
    List<Product> findPopularProducts(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND p.category.id = :categoryId " +
            "AND p.id NOT IN (:excludeProductId) " +
            "ORDER BY p.orderCount DESC")
    List<Product> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeProductId") Long excludeProductId,
            Pageable pageable);
}
