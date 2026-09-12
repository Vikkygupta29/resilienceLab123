package com.resiliencelab.order.service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent {
    private UUID eventId;
    private UUID orderId;
    private BigDecimal amount;
    private String reason;
}
