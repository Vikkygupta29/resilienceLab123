package com.resiliencelab.payment.service.messaging;

import com.resiliencelab.payment.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentFailedEvent;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final String CORRELATION_ID = "correlationId";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(
            PaymentCompletedEvent event) {

        String correlationId = MDC.get(CORRELATION_ID);

        Message<PaymentCompletedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "payment.completed")
                .setHeader(KafkaHeaders.KEY, event.getOrderId().toString())
                .setHeader("X-Correlation-ID", correlationId)
                .build();

        kafkaTemplate.send(message);

        System.out.println("Published payment.completed event");
    }

    public void publishPaymentFailed(
            PaymentFailedEvent event) {

        String correlationId = MDC.get(CORRELATION_ID);

        Message<PaymentFailedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "payment.failed")
                .setHeader(KafkaHeaders.KEY, event.getOrderId().toString())
                .setHeader("X-Correlation-ID", correlationId)
                .build();

        kafkaTemplate.send(message);

        System.out.println("Published payment.failed event");
    }
}