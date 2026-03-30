package com.punch.shop.product.service;

import com.punch.shop.common.service.BlobStorageService;
import com.punch.shop.product.dto.ProductCreateRequest;
import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.Category;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.CategoryRepository;
import com.punch.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BlobStorageService blobStorageService;

    @Transactional
    public Long createProduct(ProductCreateRequest request, MultipartFile image) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        String imageUrl = blobStorageService.upload(image);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .thumbnailImageUrl(imageUrl)
                .category(category)
                .build();

        return productRepository.save(product).getId();
    }

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
