package com.resiliencelab.inventory.service.messaging;

import com.resiliencelab.inventory.service.dto.InventoryResponse;
import com.resiliencelab.inventory.service.dto.ReserveInventoryRequest;
import com.resiliencelab.inventory.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.inventory.service.entity.ProcessedEvent;
import com.resiliencelab.inventory.service.repository.ProcessedEventRepository;
import com.resiliencelab.inventory.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
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

    private static final String CONSUMER_NAME = "inventory-service";
    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";
    private static final String EVENT_ID = "eventId";

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryFailedEventProducer inventoryFailedEventProducer;
    private final ProcessedEventRepository processedEventRepository;

    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    @Transactional
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000)
    )
    @KafkaListener(
            id = "inventoryOrderCreatedListener",
            topics = "order.created",
            groupId = "inventory-service-group"
    )
    public void consumeOrderCreated(
            OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "X-Correlation-ID", required = false) String correlationId
    ) {

        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }

        MDC.put(ORDER_ID, event.getOrderId());
        MDC.put(EVENT_ID, event.getEventId());

        try {

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

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(EVENT_ID);
        }
    }

    @DltHandler
    public void handleDlt(
            OrderCreatedEvent event,
            @Header(value = "X-Correlation-ID", required = false) String correlationId
    ) {

        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }

        MDC.put(ORDER_ID, event.getOrderId());
        MDC.put(EVENT_ID, event.getEventId());

        try {

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

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(EVENT_ID);
        }
    }
}
