package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response returned by GlobalExceptionHandler.
 * Provides detailed error information for debugging while keeping
 * raw stack traces hidden from the client (security best practice).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code */
    private int status;

    /** Machine-readable error code (e.g., "ERR_RESOURCE_NOT_FOUND") */
    private String errorCode;

    /** Human-readable error message */
    private String message;

    /** Detailed description (optional, for debugging) */
    private String details;

    /** The API path that triggered the error */
    private String path;

    /** Timestamp of when the error occurred */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Validation errors — field-level error messages for 400 responses */
    private Map<String, String> validationErrors;
}
