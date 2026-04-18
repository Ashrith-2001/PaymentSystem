package com.payment.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful login.
 * Contains the JWT token and user metadata for the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String role;
    private long expiresIn;  // Token validity in seconds
}
