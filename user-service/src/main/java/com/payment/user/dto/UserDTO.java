package com.payment.user.dto;

import com.payment.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning user details to the client.
 * Password is NEVER included (security best practice: DTO isolation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private List<AddressDTO> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
