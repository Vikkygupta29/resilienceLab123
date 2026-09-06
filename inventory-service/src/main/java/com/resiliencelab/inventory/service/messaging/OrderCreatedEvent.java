package com.resiliencelab.inventory.service.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private String eventId;
    private String orderId;
    private String productId;
    private BigDecimal amount;
    private int quantity;

}
