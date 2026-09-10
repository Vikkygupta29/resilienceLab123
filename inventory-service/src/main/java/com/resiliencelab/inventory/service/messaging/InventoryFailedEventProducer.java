package com.resiliencelab.inventory.service.messaging;

import com.resiliencelab.inventory.service.dto.event.InventoryFailedEvent;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class InventoryFailedEventProducer {

    private static final String CORRELATION_ID = "correlationId";

    private final KafkaTemplate<String, InventoryFailedEvent> kafkaTemplate;

    public InventoryFailedEventProducer(
            KafkaTemplate<String, InventoryFailedEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryFailed(
            InventoryFailedEvent event) {

        String correlationId = MDC.get(CORRELATION_ID);

        Message<InventoryFailedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "inventory.failed")
                .setHeader(KafkaHeaders.KEY, event.getOrderId())
                .setHeader("X-Correlation-ID", correlationId)
                .build();

        kafkaTemplate.send(message);

        System.out.println("Published inventory.failed event");
    }
}