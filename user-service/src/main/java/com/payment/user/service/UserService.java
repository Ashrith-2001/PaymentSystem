package com.payment.user.service;

import com.payment.common.exception.BusinessException;
import com.payment.common.exception.ResourceNotFoundException;
import com.payment.user.dto.*;
import com.payment.user.entity.Address;
import com.payment.user.entity.User;
import com.payment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Service — Core business logic for user management and authentication.
 * 
 * This service layer:
 * - Contains NO HTTP/REST concerns (clean separation per FirstPrinciples)
 * - Handles user registration, authentication, profile CRUD
 * - Uses BCrypt for password hashing
 * - Generates JWT tokens for authenticated sessions
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ==================== AUTHENTICATION ====================

    /**
     * Register a new user.
     * Validates email uniqueness and encodes the password with BCrypt.
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "User with email " + request.getEmail() + " already exists",
                    "ERR_USER_EXISTS"
            );
        }

        // Build user entity from request (password is BCrypt encoded)
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return mapToDTO(savedUser);
    }

    /**
     * Authenticate a user and return a JWT token.
     * 
     * Flow:
     * 1. Spring Security's AuthenticationManager validates credentials
     * 2. If valid, generate JWT with userId and role as extra claims
     * 3. Return LoginResponse with token and user metadata
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // AuthenticationManager delegates to UserDetailsService + PasswordEncoder
        // Throws AuthenticationException if credentials are invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication was successful
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Add custom claims to the JWT (available for all services to read)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());

        // Generate UserDetails wrapper for Spring Security
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(extraClaims, userDetails);

        log.info("User {} logged in successfully", user.getEmail());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtService.getJwtExpiration() / 1000) // Convert ms to seconds
                .build();
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToDTO(user);
    }

    /**
     * Get user by email.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToDTO(user);
    }

    /**
     * Get all users (admin only).
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile (excluding password and role changes).
     */
    @Transactional
    public UserDTO updateUser(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // Email change requires uniqueness check
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(
                        "Email " + request.getEmail() + " is already in use",
                        "ERR_EMAIL_IN_USE"
                );
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("User {} profile updated", userId);
        return mapToDTO(updatedUser);
    }

    /**
     * Deactivate a user account (soft delete).
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated", userId);
    }

    // ==================== MAPPING (Entity ↔ DTO) ====================

    /**
     * Convert User entity to UserDTO.
     * This ensures the password is NEVER exposed in API responses.
     */
    private UserDTO mapToDTO(User user) {
        List<AddressDTO> addressDTOs = user.getAddresses() != null
                ? user.getAddresses().stream().map(this::mapAddressToDTO).collect(Collectors.toList())
                : List.of();

        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .addresses(addressDTOs)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AddressDTO mapAddressToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .primary(address.isPrimary())
                .build();
    }
}
