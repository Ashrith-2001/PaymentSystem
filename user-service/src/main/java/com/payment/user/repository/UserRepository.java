package com.payment.user.repository;

import com.payment.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Spring Data JPA auto-generates the implementation at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email (used for login and duplicate detection).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists (for registration).
     */
    boolean existsByEmail(String email);
}
