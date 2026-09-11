package com.resiliencelab.inventory.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/kafka")
public class KafkaConsumerControlController {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaConsumerControlController(
            KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/inventory/pause")
    public ResponseEntity<String> pauseInventoryConsumer() {

        MessageListenerContainer container =
                registry.getListenerContainer("inventoryOrderCreatedListener");

        if (container == null) {
            return ResponseEntity.notFound().build();
        }

        container.pause();

        return ResponseEntity.ok(
                "Inventory Kafka consumer paused"
        );
    }

    @PostMapping("/inventory/resume")
    public ResponseEntity<String> resumeInventoryConsumer() {

        MessageListenerContainer container =
                registry.getListenerContainer("inventoryOrderCreatedListener");

        if (container == null) {
            return ResponseEntity.notFound().build();
        }

        container.resume();

        return ResponseEntity.ok(
                "Inventory Kafka consumer resumed"
        );
    }
}