package com.resiliencelab.order.service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestedEvent {

    private UUID eventId;
    private UUID  orderId;
    private BigDecimal amount;

}
