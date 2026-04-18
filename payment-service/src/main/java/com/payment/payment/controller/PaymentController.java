package com.payment.payment.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.payment.dto.PaymentRequest;
import com.payment.payment.dto.PaymentResponse;
import com.payment.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        HttpStatus status = response.getStatus().name().equals("COMPLETED")
                ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status)
                .body(ApiResponse.success("Payment processed", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all payments for a user")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", payments));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable Long id) {
        PaymentResponse refund = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded", refund));
    }
}
