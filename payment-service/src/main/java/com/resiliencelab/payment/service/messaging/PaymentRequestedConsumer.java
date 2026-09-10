package com.resiliencelab.payment.service.messaging;

import com.resiliencelab.payment.service.dto.PaymentRequest;
import com.resiliencelab.payment.service.dto.PaymentResponse;
import com.resiliencelab.payment.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentFailedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentRequestedEvent;
import com.resiliencelab.payment.service.repository.ProcessedEventRepository;
import com.resiliencelab.payment.service.service.PaymentService;
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
import java.util.UUID;

@Component
public class PaymentRequestedConsumer {

    private static final String CORRELATION_ID = "correlationId";

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

        try {
            System.out.println("=================================");
            System.out.println("Payment Service received payment.requested");
            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Amount: " + event.getAmount());

            UUID eventId = event.getEventId();

            int inserted = processedEventRepository.tryMarkAsProcessed(
                    eventId,
                    "payment-service",
                    LocalDateTime.now()
            );

            if (inserted == 0) {
                System.out.println("Duplicate Kafka event detected: " + eventId);
                System.out.println("Skipping payment processing.");
                return;
            }

            System.out.println("Kafka event marked as processed: "
                    + event.getEventId());

            PaymentRequest request = new PaymentRequest(
                    event.getOrderId(),
                    event.getAmount()
            );

            PaymentResponse response =
                    paymentService.processPayment(request);

            System.out.println("Payment processed successfully!");
            System.out.println("Payment Response: " + response);

            PaymentCompletedEvent completedEvent =
                    new PaymentCompletedEvent(
                            event.getOrderId(),
                            event.getAmount()
                    );

            paymentEventProducer.publishPaymentCompleted(completedEvent);

            System.out.println("=================================");

        } finally {
            MDC.remove(CORRELATION_ID);
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

        try {
            System.out.println("=================================");
            System.out.println("Payment request sent to DEAD LETTER TOPIC");
            System.out.println("Order ID: " + event.getOrderId());
            System.out.println("Amount: " + event.getAmount());

            PaymentFailedEvent failedEvent =
                    new PaymentFailedEvent(
                            event.getOrderId().toString(),
                            event.getAmount(),
                            "Payment processing failed after all retry attempts"
                    );

            paymentEventProducer.publishPaymentFailed(failedEvent);

            System.out.println("=================================");

        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}