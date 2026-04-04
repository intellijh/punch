package com.punch.shop.cart.model;

import com.punch.shop.cart.exception.CartItemNotFoundException;
import com.punch.shop.common.model.BaseEntity;
import com.punch.shop.member.model.Member;
import com.punch.shop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addQuantity(quantity),
                        () -> items.add(CartItem.builder().cart(this).product(product).quantity(quantity).build())
                );
    }

    public void updateItemQuantity(Long productId, int quantity) {
        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId))
                .updateQuantity(quantity);
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public BigDecimal getCheckedTotalPrice() {
        return items.stream()
                .filter(CartItem::isChecked)
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateItemChecked(Long productId, boolean checked) {
        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId))
                .updateChecked(checked);
    }

    public void checkAllItems(boolean checked) {
        items.forEach(item -> item.updateChecked(checked));
    }

    public int getItemCount() {
        return items.size();
    }

    public static Cart create(Member member) {
        return Cart.builder().member(member).build();
    }
}
