package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.InventoryReservedEvent;
import com.resiliencelab.order.service.dto.event.PaymentRequestedEvent;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import com.resiliencelab.order.service.repository.OrderRepository;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryReservedConsumer {

    private static final String CORRELATION_ID = "correlationId";

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
            groupId = "order-service-inventory-group"
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

        try {
            System.out.println("=================================");
            System.out.println("Order Service received inventory.reserved");
            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Product ID: " + event.getProductId());
            System.out.println("Quantity: " + event.getQuantity());

            UUID orderId = UUID.fromString(event.getOrderId());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.INVENTORY_RESERVED);

            orderRepository.save(order);

            System.out.println("Order status updated to INVENTORY_RESERVED");

            PaymentRequestedEvent paymentEvent =
                    new PaymentRequestedEvent(
                            UUID.randomUUID(),
                            order.getId(),
                            order.getAmount()
                    );

            paymentEventProducer.publishPaymentRequested(paymentEvent);

            System.out.println("=================================");

        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}