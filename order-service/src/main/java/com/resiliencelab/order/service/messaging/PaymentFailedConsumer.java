package com.resiliencelab.order.service.messaging;

import com.resiliencelab.order.service.dto.event.PaymentFailedEvent;
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
public class PaymentFailedConsumer {

    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";
    private static final String EVENT_ID = "eventId";

    private static final Logger log =
            LoggerFactory.getLogger(PaymentFailedConsumer.class);

    private final OrderRepository orderRepository;

    public PaymentFailedConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "order-service-payment-failed-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void consumePaymentFailed(
            PaymentFailedEvent event,
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
                    "Received payment.failed event: amount={}, reason={}",
                    event.getAmount(),
                    event.getReason()
            );

            UUID orderId = event.getOrderId();

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found: " + orderId
                    ));

            order.setStatus(OrderStatus.FAILED);

            orderRepository.save(order);

            log.info("Order status updated to FAILED");

        } finally {
            MDC.remove(EVENT_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(CORRELATION_ID);
        }
    }
}