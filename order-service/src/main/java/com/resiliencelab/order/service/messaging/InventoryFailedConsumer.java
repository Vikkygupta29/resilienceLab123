
        package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import com.resiliencelab.order.service.repository.OrderRepository;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryFailedConsumer {

    private static final String CORRELATION_ID = "correlationId";

    private final OrderRepository orderRepository;

    public InventoryFailedConsumer(
            OrderRepository orderRepository) {

        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "inventory.failed",
            groupId = "order-service-inventory-failed-group",
            containerFactory = "inventoryFailedKafkaListenerContainerFactory"
    )
    public void consumeInventoryFailed(
            InventoryFailedEvent event,
            @Header(
                    value = "X-Correlation-ID",
                    required = false
            ) String correlationId) {

        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }

        MDC.put("orderId", event.getOrderId());

        try {
            System.out.println("=================================");
            System.out.println("Order Service received inventory.failed");
            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Product ID: " + event.getProductId());
            System.out.println("Quantity: " + event.getQuantity());
            System.out.println("Reason: " + event.getReason());

            UUID orderId = UUID.fromString(event.getOrderId());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.FAILED);

            orderRepository.save(order);

            System.out.println("Order status updated to FAILED");
            System.out.println("=================================");

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove("orderId");
        }
    }
}