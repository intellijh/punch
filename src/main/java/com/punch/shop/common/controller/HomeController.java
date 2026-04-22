package com.punch.shop.common.controller;

import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.product.service.ProductService;
import com.punch.shop.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final int HOME_RECOMMENDED_LIMIT = 8;

    private final ProductService productService;
    private final RecommendationService recommendationService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        Long memberId = principal != null ? principal.getMemberId() : null;
        model.addAttribute("categories", productService.getCategories());
        model.addAttribute("recommendedProducts",
                recommendationService.getRecommendedProducts(memberId, HOME_RECOMMENDED_LIMIT));
        return "index";
    }
}
