package com.payment.user.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.user.dto.LoginRequest;
import com.payment.user.dto.LoginResponse;
import com.payment.user.dto.RegisterRequest;
import com.payment.user.dto.UserDTO;
import com.payment.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller — Handles registration and login.
 * 
 * All endpoints under /api/auth are PUBLIC (no JWT required).
 * This is configured in SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    /**
     * Register a new user.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with CUSTOMER role")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        UserDTO user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * Authenticate a user and return JWT token.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }
}
