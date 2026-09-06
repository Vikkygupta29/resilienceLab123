package com.resiliencelab.inventory.service.dto;

import com.resiliencelab.inventory.service.entity.Inventory;

import java.io.Serializable;
import java.time.Instant;

public record InventoryResponse(
        String productId,
        int availableQuantity,
        int reservedQuantity,
        Instant  updatedAt
) implements Serializable {

    public static InventoryResponse from(Inventory inventory){
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getAvailabelQuantity(),
                inventory.getReservedQuantity(),
                inventory.getUpdatedAt()
        );
    }
}
