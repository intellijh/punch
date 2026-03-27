package com.punch.shop.order.controller;

import com.punch.shop.cart.dto.CartResponse;
import com.punch.shop.cart.service.CartService;
import com.punch.shop.member.dto.AddressCreateRequest;
import com.punch.shop.member.dto.AddressResponse;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.AddressService;
import com.punch.shop.order.dto.OrderCreateRequest;
import com.punch.shop.order.dto.OrderListItemResponse;
import com.punch.shop.order.dto.OrderResponse;
import com.punch.shop.order.model.PaymentMethod;
import com.punch.shop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressService addressService;

    @GetMapping("/checkout")
    public String checkoutForm(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        CartResponse cart = cartService.getCart(principal.getMemberId());
        List<AddressResponse> addresses = addressService.getAddresses(principal.getMemberId());
        AddressResponse selectedAddress = addresses.stream()
                .filter(AddressResponse::isDefaultAddress)
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        if (selectedAddress != null) {
            orderCreateRequest.setAddressId(selectedAddress.getId());
        }

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("selectedAddress", selectedAddress);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("addressCreateRequest", new AddressCreateRequest());
        return "order/checkout";
    }

    @PostMapping
    public String createOrder(@AuthenticationPrincipal MemberPrincipal principal,
                              @Valid @ModelAttribute OrderCreateRequest orderCreateRequest,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            CartResponse cart = cartService.getCart(principal.getMemberId());
            List<AddressResponse> addresses = addressService.getAddresses(principal.getMemberId());
            AddressResponse selectedAddress = addresses.stream()
                    .filter(a -> a.getId().equals(orderCreateRequest.getAddressId()))
                    .findFirst()
                    .orElse(addresses.isEmpty() ? null : addresses.get(0));

            model.addAttribute("cart", cart);
            model.addAttribute("addresses", addresses);
            model.addAttribute("selectedAddress", selectedAddress);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("addressCreateRequest", new AddressCreateRequest());
            return "order/checkout";
        }

        OrderResponse order = orderService.createOrder(principal.getMemberId(), orderCreateRequest);
        log.info("주문 완료: memberId={}, orderId={}", principal.getMemberId(), order.getOrderId());
        return "redirect:/orders/" + order.getOrderId() + "/confirmation";
    }

    @GetMapping("/{orderId}/confirmation")
    public String confirmation(@AuthenticationPrincipal MemberPrincipal principal,
                               @PathVariable Long orderId,
                               Model model) {
        OrderResponse order = orderService.getOrder(principal.getMemberId(), orderId);
        model.addAttribute("order", order);
        model.addAttribute("confirmed", true);
        return "order/detail";
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@AuthenticationPrincipal MemberPrincipal principal,
                              @PathVariable Long orderId,
                              Model model) {
        OrderResponse order = orderService.getOrder(principal.getMemberId(), orderId);
        model.addAttribute("order", order);
        return "order/detail";
    }

    @GetMapping
    public String orderList(@AuthenticationPrincipal MemberPrincipal principal,
                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                            Model model) {
        Page<OrderListItemResponse> orders = orderService.getOrders(principal.getMemberId(), pageable);
        model.addAttribute("orders", orders);
        return "order/list";
    }
}
