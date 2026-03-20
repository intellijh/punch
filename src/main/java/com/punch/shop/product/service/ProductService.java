package com.punch.shop.product.service;

import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(ProductResponse::from);
    }

    public ProductDetailResponse getProduct(Long id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .map(ProductDetailResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
