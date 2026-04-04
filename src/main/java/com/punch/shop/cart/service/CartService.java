package com.punch.shop.cart.service;

import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.exception.CartNotFoundException;
import com.punch.shop.cart.model.Cart;
import com.punch.shop.cart.repository.CartRepository;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.product.exception.ProductNotFoundException;
import com.punch.shop.product.model.Product;
import com.punch.shop.product.model.ProductStatus;
import com.punch.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(CartResponse::from)
                .orElse(CartResponse.empty());
    }

    @Transactional
    public void addItem(Long memberId, Long productId, int quantity) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.getReferenceById(memberId);
                    return cartRepository.save(Cart.create(member));
                });

        cart.addItem(product, quantity);
    }

    @Transactional
    public void updateQuantity(Long memberId, Long productId, int quantity) {
        Cart cart = getCartOrThrow(memberId);
        cart.updateItemQuantity(productId, quantity);
    }

    @Transactional
    public void removeItem(Long memberId, Long productId) {
        Cart cart = getCartOrThrow(memberId);
        cart.removeItem(productId);
    }

    @Transactional
    public void updateItemChecked(Long memberId, Long productId, boolean checked) {
        Cart cart = getCartOrThrow(memberId);
        cart.updateItemChecked(productId, checked);
    }

    @Transactional
    public void checkAllItems(Long memberId, boolean checked) {
        Cart cart = getCartOrThrow(memberId);
        cart.checkAllItems(checked);
    }

    public int getCartItemCount(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(Cart::getItemCount)
                .orElse(0);
    }

    private Cart getCartOrThrow(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException());
    }
}
