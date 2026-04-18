package com.payment.common.exception;

import com.payment.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler applied to all REST controllers.
 * Converts exceptions into standardized ErrorResponse objects.
 * 
 * Key principles (from FirstPrinciplesForABackendSystem.md):
 * - Never expose raw stack traces to the client
 * - Use structured, consistent error responses
 * - Log the full exception server-side for debugging
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle resource not found (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("ERR_RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle business logic exceptions (400).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception: {} [code={}]", ex.getMessage(), ex.getErrorCode());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Map specific error codes to appropriate HTTP status codes
        if ("ERR_INSUFFICIENT_FUNDS".equals(ex.getErrorCode())) {
            status = HttpStatus.PAYMENT_REQUIRED; // 402
        } else if ("ERR_INSUFFICIENT_STOCK".equals(ex.getErrorCode()) ||
                   "ERR_DUPLICATE_REQUEST".equals(ex.getErrorCode())) {
            status = HttpStatus.CONFLICT; // 409
        }

        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }

    /**
     * Handle validation errors from @Valid annotations (400).
     * Returns field-level error details for each invalid field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed for request to {}", request.getRequestURI());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("ERR_VALIDATION_FAILED")
                .message("Validation failed. Check 'validationErrors' for details.")
                .path(request.getRequestURI())
                .validationErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions (400).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("ERR_INVALID_ARGUMENT")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all handler for unexpected exceptions (500).
     * Logs the full stack trace server-side but returns a generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Log full stack trace for debugging — never exposed to client
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("ERR_INTERNAL")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
