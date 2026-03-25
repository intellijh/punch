package com.punch.shop.cart.controller;

import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.model.MemberPrincipal;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String cart(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        CartResponse cart = cartService.getCart(principal.getMemberId());
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/items")
    public String addItem(@AuthenticationPrincipal MemberPrincipal principal,
                          @RequestParam Long productId,
                          @RequestParam(defaultValue = "1") @Min(value = 1, message = "수량은 1 이상이어야 합니다.") int quantity) {
        cartService.addItem(principal.getMemberId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}")
    public String updateQuantity(@AuthenticationPrincipal MemberPrincipal principal,
                                 @PathVariable Long productId,
                                 @RequestParam @Min(value = 1, message = "수량은 1 이상이어야 합니다.") int quantity) {
        cartService.updateQuantity(principal.getMemberId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/delete")
    public String removeItem(@AuthenticationPrincipal MemberPrincipal principal,
                             @PathVariable Long productId) {
        cartService.removeItem(principal.getMemberId(), productId);
        return "redirect:/cart";
    }
}
