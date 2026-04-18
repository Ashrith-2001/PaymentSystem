package com.payment.order.service;

import com.payment.common.enums.OrderStatus;
import com.payment.common.event.OrderEvent;
import com.payment.common.exception.BusinessException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.order.dto.CreateOrderRequest;
import com.payment.order.dto.OrderDTO;
import com.payment.order.entity.Order;
import com.payment.order.entity.OrderItem;
import com.payment.order.kafka.OrderEventProducer;
import com.payment.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service — Core business logic for order management.
 * 
 * This service acts as the SAGA orchestrator:
 * - Creates orders and publishes ORDER_CREATED events
 * - Listens for INVENTORY_RESERVED and PAYMENT_COMPLETED events (via Kafka)
 * - Updates order status accordingly
 * - Triggers compensating transactions on failure
 * 
 * Optimistic Locking: The Order entity uses @Version to prevent concurrent
 * status updates from conflicting (e.g., two Kafka events arriving at the same time).
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    /**
     * Create a new order and kickoff the SAGA.
     * 
     * Steps:
     * 1. Build Order entity from request
     * 2. Calculate total amount
     * 3. Save with PENDING status
     * 4. Publish ORDER_CREATED event to Kafka → triggers Inventory reservation
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating order for user {}", request.getUserId());

        // Build order entity
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .build();

        // Add items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        order.updateStatus(OrderStatus.PENDING, "Order created");

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} created with total {}", savedOrder.getId(), totalAmount);

        // Publish ORDER_CREATED event to Kafka → triggers SAGA
        List<OrderEvent.OrderItemEvent> eventItems = request.getItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        OrderEvent event = OrderEvent.orderCreated(
                savedOrder.getId(), savedOrder.getUserId(),
                savedOrder.getTotalAmount(), eventItems);
        eventProducer.publishOrderEvent(event);

        return mapToDTO(savedOrder);
    }

    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDTO(order);
    }

    /**
     * Get all orders for a user.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update order status (called by SAGA event handlers).
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        log.info("Updating order {} status: {} → {} (reason: {})",
                orderId, order.getStatus(), newStatus, reason);

        order.updateStatus(newStatus, reason);
        orderRepository.save(order);
    }

    /**
     * Cancel an order (user-initiated).
     * Only PENDING orders can be cancelled directly.
     * For orders with reserved inventory, triggers compensation.
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Cannot cancel an order in " + order.getStatus() + " status",
                    "ERR_INVALID_CANCELLATION");
        }

        order.updateStatus(OrderStatus.CANCELLED, reason);
        Order saved = orderRepository.save(order);

        // Publish cancellation event → triggers inventory release
        OrderEvent event = OrderEvent.orderCancelled(orderId, order.getUserId(), reason);
        eventProducer.publishOrderEvent(event);

        log.info("Order {} cancelled: {}", orderId, reason);
        return mapToDTO(saved);
    }

    // ==================== Mapping ====================

    private OrderDTO mapToDTO(Order order) {
        List<OrderDTO.OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderDTO.OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
