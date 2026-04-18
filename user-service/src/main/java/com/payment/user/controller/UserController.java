package com.payment.user.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.user.dto.RegisterRequest;
import com.payment.user.dto.UserDTO;
import com.payment.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller — Protected endpoints for user profile management.
 * 
 * All endpoints require JWT authentication (configured in SecurityConfig).
 * Some endpoints are restricted to ADMIN role via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Get user by ID.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * Get all users (admin only).
     * GET /api/users/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Update user profile.
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest request) {
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Deactivate user account (admin only — soft delete).
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }
}
