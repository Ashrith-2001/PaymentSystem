package com.payment.inventory.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private int totalQuantity;
    private int availableQuantity;
    private int reservedQuantity;
}
