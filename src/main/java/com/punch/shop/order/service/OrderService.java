package com.punch.shop.order.service;

import com.punch.shop.cart.exception.EmptyCartException;
import com.punch.shop.cart.model.Cart;
import com.punch.shop.cart.repository.CartRepository;
import com.punch.shop.member.exception.AddressNotFoundException;
import com.punch.shop.member.model.Address;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.AddressRepository;
import com.punch.shop.member.repository.MemberRepository;
import com.punch.shop.order.dto.OrderCreateRequest;
import com.punch.shop.order.dto.OrderListItemResponse;
import com.punch.shop.order.dto.OrderResponse;
import com.punch.shop.order.exception.OrderNotFoundException;
import com.punch.shop.order.model.Order;
import com.punch.shop.order.repository.OrderRepository;
import com.punch.shop.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(EmptyCartException::new);

        Address address = addressRepository.findByIdAndMemberId(request.getAddressId(), memberId)
                .orElseThrow(() -> new AddressNotFoundException(request.getAddressId()));

        Member member = memberRepository.getReferenceById(memberId);
        Order order = Order.create(member, address, request.getPaymentMethod());

        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            product.reduceStock(cartItem.getQuantity());
            product.increaseOrderCount(cartItem.getQuantity());
            order.addItem(product, cartItem.getQuantity());
        });

        Order saved = orderRepository.save(order);
        cart.getItems().clear();

        return OrderResponse.from(saved);
    }

    public Page<OrderListItemResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable)
                .map(OrderListItemResponse::from);
    }

    public OrderResponse getOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMemberId(orderId, memberId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
