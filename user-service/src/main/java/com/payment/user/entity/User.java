package com.payment.user.entity;

import com.payment.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity — represents a registered user in the system.
 * 
 * Auditing: Automatically tracks creation and modification timestamps.
 * Relationships: One-to-many with Address (a user can have multiple addresses).
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt encoded

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * One user can have multiple addresses.
     * CascadeType.ALL: Creating a user with addresses persists both.
     * orphanRemoval: Removing an address from the list deletes it from DB.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ---- Helper methods for bidirectional relationship management ----

    /**
     * Adds an address and sets the back-reference.
     */
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    /**
     * Removes an address and clears the back-reference.
     */
    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }
}
