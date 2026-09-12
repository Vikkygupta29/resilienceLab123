
        package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.InventoryFailedEvent;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import com.resiliencelab.order.service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryFailedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryFailedConsumer.class);

    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";

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

        MDC.put(ORDER_ID, event.getOrderId());

        try {
            log.info(
                    "Received inventory.failed event. productId={}, quantity={}, reason={}",
                    event.getProductId(),
                    event.getQuantity(),
                    event.getReason()
            );

            UUID orderId = UUID.fromString(event.getOrderId());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.FAILED);

            orderRepository.save(order);

            log.info("Order status updated to FAILED");

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(ORDER_ID);
        }
    }
}
