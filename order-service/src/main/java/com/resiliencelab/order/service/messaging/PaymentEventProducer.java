package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.PaymentRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentRequestedEvent> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, PaymentRequestedEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentRequested(
            PaymentRequestedEvent event) {

        kafkaTemplate.send(
                "payment.requested",
                event.getOrderId().toString(),
                event
        );

        log.info(
                "Published payment.requested event: orderId={}, amount={}",
                event.getOrderId(),
                event.getAmount()
        );
    }
}