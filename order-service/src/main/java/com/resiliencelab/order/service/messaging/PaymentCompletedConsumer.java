package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import com.resiliencelab.order.service.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentCompletedConsumer {

    private static final String CORRELATION_ID = "correlationId";

    private final OrderRepository orderRepository;
    private final Counter ordersConfirmedCounter;

    public PaymentCompletedConsumer(
            OrderRepository orderRepository,
            MeterRegistry meterRegistry) {

        this.orderRepository = orderRepository;

        this.ordersConfirmedCounter = Counter.builder("orders_confirmed_total")
                .description("Total number of orders confirmed")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = "payment.completed",
            groupId = "order-service-payment-group",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consumePaymentCompleted(
            PaymentCompletedEvent event,
            @Header(
                    value = "X-Correlation-ID",
                    required = false
            ) String correlationId) {

        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }

        try {
            System.out.println("=================================");
            System.out.println("Order Service received payment.completed");
            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Amount: " + event.getAmount());

            UUID orderId = event.getOrderId();

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.CONFIRMED);

            orderRepository.save(order);

            ordersConfirmedCounter.increment();

            System.out.println("Order status updated to CONFIRMED");
            System.out.println("=================================");

        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}