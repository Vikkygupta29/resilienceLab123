package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.order.service.entity.Order;
import com.resiliencelab.order.service.enums.OrderStatus;
import com.resiliencelab.order.service.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentCompletedConsumer {

    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";
    private static final String EVENT_ID = "eventId";

    private static final Logger log =
            LoggerFactory.getLogger(PaymentCompletedConsumer.class);

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

        MDC.put(ORDER_ID, event.getOrderId().toString());
        MDC.put(EVENT_ID, event.getEventId().toString());

        try {
            log.info(
                    "Received payment.completed event: amount={}",
                    event.getAmount()
            );

            UUID orderId = event.getOrderId();

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.CONFIRMED);

            orderRepository.save(order);

            ordersConfirmedCounter.increment();

            log.info("Order status updated to CONFIRMED");

        } finally {
            MDC.remove(EVENT_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(CORRELATION_ID);
        }
    }
}