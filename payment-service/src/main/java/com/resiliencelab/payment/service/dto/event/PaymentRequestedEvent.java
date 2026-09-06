package com.resiliencelab.payment.service.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
public class PaymentRequestedEvent {
    private UUID eventId;
    private UUID orderId;
    private BigDecimal amount;
}
