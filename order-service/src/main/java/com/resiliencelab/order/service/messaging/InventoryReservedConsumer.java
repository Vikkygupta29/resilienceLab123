package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.InventoryReservedEvent;
import com.resiliencelab.order.service.dto.event.PaymentRequestedEvent;
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
public class InventoryReservedConsumer {

    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";
    private static final String EVENT_ID = "eventId";

    private static final Logger log =
            LoggerFactory.getLogger(InventoryReservedConsumer.class);

    private final OrderRepository orderRepository;
    private final PaymentEventProducer paymentEventProducer;

    public InventoryReservedConsumer(
            OrderRepository orderRepository,
            PaymentEventProducer paymentEventProducer) {

        this.orderRepository = orderRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "order-service-inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeInventoryReserved(
            InventoryReservedEvent event,
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
                    "Received inventory.reserved event: productId={}, quantity={}",
                    event.getProductId(),
                    event.getQuantity()
            );

            UUID orderId = UUID.fromString(event.getOrderId());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.INVENTORY_RESERVED);

            orderRepository.save(order);

            log.info("Order status updated to INVENTORY_RESERVED");

            PaymentRequestedEvent paymentEvent =
                    new PaymentRequestedEvent(
                            UUID.randomUUID(),
                            order.getId(),
                            order.getAmount()
                    );

            MDC.put(EVENT_ID, paymentEvent.getEventId().toString());

            paymentEventProducer.publishPaymentRequested(paymentEvent);

            log.info("Payment requested event published");

        } finally {
            MDC.remove(EVENT_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(CORRELATION_ID);
        }
    }
}