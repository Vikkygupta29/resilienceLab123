package com.resiliencelab.inventory.service.messaging;

import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private static final String CORRELATION_ID = "correlationId";

    private final KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;

    public InventoryEventProducer(
            KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryReserved(
            InventoryReservedEvent event) {

        String correlationId = MDC.get(CORRELATION_ID);

        Message<InventoryReservedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "inventory.reserved")
                .setHeader(KafkaHeaders.KEY, event.getOrderId())
                .setHeader("X-Correlation-ID", correlationId)
                .build();

        kafkaTemplate.send(message);

        System.out.println("Published inventory.reserved event");
    }
}