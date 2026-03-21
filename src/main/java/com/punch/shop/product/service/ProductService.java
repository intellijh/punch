package com.punch.shop.product.service;

import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> searchProducts(String keyword, Long categoryId, Pageable pageable) {
        return productRepository.search(keyword, categoryId, pageable)
                .map(ProductResponse::from);
    }

    public ProductDetailResponse getProduct(Long id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .map(ProductDetailResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
