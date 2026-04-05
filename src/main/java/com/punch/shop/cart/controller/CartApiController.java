package com.punch.shop.cart.controller;

import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.model.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @PatchMapping("/items/{productId}/quantity")
    public ResponseEntity<CartResponse> updateQuantity(@AuthenticationPrincipal MemberPrincipal principal,
                                                       @PathVariable Long productId,
                                                       @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(principal.getMemberId(), productId, body.get("quantity"));
        CartResponse cart = cartService.getCart(principal.getMemberId());
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/items/{productId}/checked")
    public ResponseEntity<CartResponse> updateItemChecked(@AuthenticationPrincipal MemberPrincipal principal,
                                                          @PathVariable Long productId,
                                                          @RequestBody Map<String, Boolean> body) {
        cartService.updateItemChecked(principal.getMemberId(), productId, body.get("checked"));
        CartResponse cart = cartService.getCart(principal.getMemberId());
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/check-all")
    public ResponseEntity<CartResponse> checkAllItems(@AuthenticationPrincipal MemberPrincipal principal,
                                                      @RequestBody Map<String, Boolean> body) {
        cartService.checkAllItems(principal.getMemberId(), body.get("checked"));
        CartResponse cart = cartService.getCart(principal.getMemberId());
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(cartService.getCartItemCount(principal.getMemberId()));
    }
}
