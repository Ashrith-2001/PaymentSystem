package com.payment.payment.repository;

import com.payment.payment.entity.Payment;
import com.payment.common.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payment by order ID with pessimistic lock.
     * Prevents concurrent payment processing for the same order.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdForUpdate(@Param("orderId") Long orderId);
}
