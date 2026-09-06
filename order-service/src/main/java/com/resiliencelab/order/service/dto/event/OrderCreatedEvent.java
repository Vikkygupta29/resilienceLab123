package com.resiliencelab.order.service.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String productId,
        BigDecimal amount,
        int quantity
) {
}
