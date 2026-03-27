package com.punch.shop.order.model;

import com.punch.shop.common.model.BaseEntity;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.product.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 5)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    private String addressDetail;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    public void addItem(Product product, int quantity) {
        OrderItem item = OrderItem.create(this, product, quantity);
        items.add(item);
        this.totalPrice = totalPrice.add(item.getSubtotal());
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public static Order create(Member member, Address shippingAddress, PaymentMethod paymentMethod) {
        return Order.builder()
                .member(member)
                .paymentMethod(paymentMethod)
                .recipientName(shippingAddress.getRecipientName())
                .phone(shippingAddress.getPhone())
                .zipCode(shippingAddress.getZipCode())
                .address(shippingAddress.getAddress())
                .addressDetail(shippingAddress.getAddressDetail())
                .totalPrice(BigDecimal.ZERO)
                .build();
    }
}
