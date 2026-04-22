package com.punch.shop.product.controller;

import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.product.dto.ProductDetailResponse;
import com.punch.shop.product.dto.ProductResponse;
import com.punch.shop.product.service.ProductService;
import com.punch.shop.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int RELATED_PRODUCT_LIMIT = 4;

    private final ProductService productService;
    private final RecommendationService recommendationService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "popular") String sort,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        Pageable pageable = PageRequest.of(Math.max(0, page), DEFAULT_PAGE_SIZE, toSort(sort));
        Page<ProductResponse> products = productService.searchProducts(keyword, categoryId, pageable);
        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", productService.getCategories());
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal MemberPrincipal principal,
                         Model model) {
        ProductDetailResponse product = productService.getProduct(id);

        if (principal != null) {
            recommendationService.recordView(principal.getMemberId(), id);
        }

        List<ProductResponse> relatedProducts = recommendationService.getRelatedProducts(
                product.getId(), product.getCategoryId(), RELATED_PRODUCT_LIMIT);

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        return "product/detail";
    }

    private Sort toSort(String sort) {
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("orderCount").descending();
        };
    }
}
