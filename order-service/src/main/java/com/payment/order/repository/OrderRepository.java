package com.payment.order.repository;

import com.payment.common.enums.OrderStatus;
import com.payment.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}
