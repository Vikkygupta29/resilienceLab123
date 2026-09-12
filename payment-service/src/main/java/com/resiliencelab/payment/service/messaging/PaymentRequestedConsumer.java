package com.resiliencelab.payment.service.messaging;

import com.resiliencelab.payment.service.dto.PaymentRequest;
import com.resiliencelab.payment.service.dto.PaymentResponse;
import com.resiliencelab.payment.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentFailedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentRequestedEvent;
import com.resiliencelab.payment.service.repository.ProcessedEventRepository;
import com.resiliencelab.payment.service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentRequestedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentRequestedConsumer.class);

    private static final String CORRELATION_ID = "correlationId";
    private static final String ORDER_ID = "orderId";
    private static final String EVENT_ID = "eventId";

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentRequestedConsumer(
            PaymentService paymentService,
            PaymentEventProducer paymentEventProducer,
            ProcessedEventRepository processedEventRepository) {

        this.paymentService = paymentService;
        this.paymentEventProducer = paymentEventProducer;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000)
    )
    @KafkaListener(
            topics = "payment.requested",
            groupId = "payment-service-group"
    )
    public void consumePaymentRequested(
            PaymentRequestedEvent event,
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
                    "Received payment.requested event. amount={}",
                    event.getAmount()
            );

            UUID eventId = event.getEventId();

            int inserted = processedEventRepository.tryMarkAsProcessed(
                    eventId,
                    "payment-service",
                    LocalDateTime.now()
            );

            if (inserted == 0) {
                log.info(
                        "Duplicate Kafka event detected. Skipping payment processing."
                );
                return;
            }

            log.info("Kafka event marked as processed");

            PaymentRequest request = new PaymentRequest(
                    event.getOrderId(),
                    event.getAmount()
            );

            PaymentResponse response =
                    paymentService.processPayment(request);

            log.info(
                    "Payment processed successfully. response={}",
                    response
            );

            PaymentCompletedEvent completedEvent =
                    new PaymentCompletedEvent(
                            UUID.randomUUID(),
                            event.getOrderId(),
                            event.getAmount()
                    );

            paymentEventProducer.publishPaymentCompleted(completedEvent);

            log.info("payment.completed event published");

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(EVENT_ID);
        }
    }

    @DltHandler
    public void handleDlt(
            PaymentRequestedEvent event,
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
            log.error(
                    "Payment request sent to DEAD LETTER TOPIC. amount={}",
                    event.getAmount()
            );

            PaymentFailedEvent failedEvent =
                    new PaymentFailedEvent(
                            event.getOrderId().toString(),
                            event.getAmount(),
                            "Payment processing failed after all retry attempts"
                    );

            paymentEventProducer.publishPaymentFailed(failedEvent);

            log.info("payment.failed event published");

        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(ORDER_ID);
            MDC.remove(EVENT_ID);
        }
    }
}
