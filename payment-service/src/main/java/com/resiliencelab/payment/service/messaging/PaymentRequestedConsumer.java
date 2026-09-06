package com.resiliencelab.payment.service.messaging;

import org.springframework.transaction.annotation.Transactional;
import com.resiliencelab.payment.service.dto.PaymentRequest;
import com.resiliencelab.payment.service.dto.PaymentResponse;
import com.resiliencelab.payment.service.dto.event.PaymentCompletedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentFailedEvent;
import com.resiliencelab.payment.service.dto.event.PaymentRequestedEvent;
import com.resiliencelab.payment.service.repository.ProcessedEventRepository;
import com.resiliencelab.payment.service.service.PaymentService;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentRequestedConsumer {

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
            PaymentRequestedEvent event) {

        System.out.println("=================================");
        System.out.println("Payment Service received payment.requested");
        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Amount: " + event.getAmount());

        UUID eventId = event.getEventId();

        // Atomically mark event as processed
        int inserted = processedEventRepository.tryMarkAsProcessed(
                eventId,
                "payment-service",
                LocalDateTime.now()
        );

        // 0 means another consumer already processed this event
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
    }

    @DltHandler
    public void handleDlt(PaymentRequestedEvent event) {

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
    }
}