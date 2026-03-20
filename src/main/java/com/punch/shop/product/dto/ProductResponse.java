package com.punch.shop.product.dto;

import com.punch.shop.product.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String thumbnailImageUrl;
    private String categoryName;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnailImageUrl(product.getThumbnailImageUrl())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
