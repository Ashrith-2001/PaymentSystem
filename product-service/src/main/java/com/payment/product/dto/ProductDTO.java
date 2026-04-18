package com.payment.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for creating/updating products.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private String sku;
    private String imageUrl;
    private boolean active;
    private Long categoryId;
    private String categoryName;
}
