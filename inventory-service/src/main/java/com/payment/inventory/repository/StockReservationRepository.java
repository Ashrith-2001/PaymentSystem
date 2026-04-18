package com.payment.inventory.repository;

import com.payment.inventory.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderIdAndStatus(Long orderId, String status);

    List<StockReservation> findByOrderId(Long orderId);
}
