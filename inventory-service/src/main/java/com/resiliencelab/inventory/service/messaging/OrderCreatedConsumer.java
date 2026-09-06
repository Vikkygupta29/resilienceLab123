package com.resiliencelab.inventory.service.messaging;

import com.resiliencelab.inventory.service.dto.InventoryResponse;
import com.resiliencelab.inventory.service.dto.ReserveInventoryRequest;
import com.resiliencelab.inventory.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.inventory.service.entity.ProcessedEvent;
import com.resiliencelab.inventory.service.repository.ProcessedEventRepository;
import com.resiliencelab.inventory.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryFailedEventProducer inventoryFailedEventProducer;
    private final ProcessedEventRepository processedEventRepository;
    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    private static final String CONSUMER_NAME = "inventory-service";


    @Transactional
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000)
    )
    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-service-group"
    )
    public void consumeOrderCreated(OrderCreatedEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
                                    ) {


        System.out.println("Kafka topic: " + topic);

        String eventId = event.getEventId();

        if (processedEventRepository.existsByEventIdAndConsumerName(
                eventId,
                CONSUMER_NAME
        )) {
            System.out.println("Duplicate Kafka event detected: " + eventId);
            System.out.println("Skipping inventory reservation.");
            return;
        }




        int attempt = attemptCounter.incrementAndGet();

        System.out.println("Kafka processing attempt: " + attempt);

//        if (true) {
//            throw new RuntimeException("Simulated temporary Kafka failure");
//        }
//
//        if (attempt < 3) {
//            throw new RuntimeException("Simulated temporary Kafka failure");
//        }

        System.out.println("=================================");
        System.out.println("Inventory Service received order.created");
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Product ID: " + event.getProductId());
        System.out.println("Quantity: " + event.getQuantity());
        System.out.println("Amount: " + event.getAmount());

        ReserveInventoryRequest request =
                new ReserveInventoryRequest(event.getQuantity());

        InventoryResponse response =
                inventoryService.reserveInventory(
                        event.getProductId(),
                        request
                );

        ProcessedEvent processedEvent = new ProcessedEvent(
                event.getEventId(),
                CONSUMER_NAME,
                LocalDateTime.now()
        );

        processedEventRepository.save(processedEvent);

        System.out.println("Kafka event marked as processed: "
                + event.getEventId());

        System.out.println("Inventory reserved successfully!");
        System.out.println("Available Quantity: "
                + response.availableQuantity());
        System.out.println("Reserved Quantity: "
                + response.reservedQuantity());

        InventoryReservedEvent reservedEvent =
                new InventoryReservedEvent(
                        event.getOrderId(),
                        event.getProductId(),
                        event.getQuantity()
                );

        inventoryEventProducer.publishInventoryReserved(reservedEvent);

        System.out.println("=================================");
    }



    @DltHandler
    public void handleDlt(OrderCreatedEvent event) {

        System.out.println("=================================");
        System.out.println("Order sent to DEAD LETTER TOPIC");
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Product ID: " + event.getProductId());
        System.out.println("Quantity: " + event.getQuantity());

        InventoryFailedEvent failedEvent =
                new InventoryFailedEvent(
                        event.getOrderId(),
                        event.getProductId(),
                        event.getQuantity(),
                        "Inventory processing failed after all retry attempts"
                );

        inventoryFailedEventProducer.publishInventoryFailed(failedEvent);

        System.out.println("=================================");
    }
}