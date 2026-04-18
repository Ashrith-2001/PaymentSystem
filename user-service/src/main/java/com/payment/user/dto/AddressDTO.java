package com.payment.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for address input and output.
 * Used for both creating addresses and returning address data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;

    @NotBlank(message = "Street is required")
    @Size(max = 255)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Zip code is required")
    @Size(max = 10)
    private String zipCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    private boolean primary;
}
