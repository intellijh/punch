package com.punch.shop.product.repository;

import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);
}
