package com.punch.shop.order.repository;

import com.punch.shop.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    Optional<Order> findByIdAndMemberId(Long id, Long memberId);
}
