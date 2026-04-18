package com.payment.inventory.controller;

import com.payment.common.dto.ApiResponse;
import com.payment.inventory.dto.InventoryDTO;
import com.payment.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Add inventory for a product")
    public ResponseEntity<ApiResponse<InventoryDTO>> addInventory(@RequestBody InventoryDTO dto) {
        InventoryDTO created = inventoryService.addInventory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory added", created));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<ApiResponse<InventoryDTO>> getInventory(@PathVariable Long productId) {
        InventoryDTO inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved", inventory));
    }

    @PutMapping("/product/{productId}/add")
    @Operation(summary = "Add stock to a product")
    public ResponseEntity<ApiResponse<InventoryDTO>> addStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        InventoryDTO updated = inventoryService.updateStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", updated));
    }
}
