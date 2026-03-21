package com.punch.shop.product.repository;

import com.punch.shop.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> search(String keyword, Long categoryId, Pageable pageable);
}
