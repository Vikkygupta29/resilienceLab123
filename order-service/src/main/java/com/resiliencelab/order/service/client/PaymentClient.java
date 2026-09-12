package com.resiliencelab.order.service.client;

import com.resiliencelab.order.service.dto.client.PaymentRequest;
import com.resiliencelab.order.service.dto.client.PaymentResponse;
import com.resiliencelab.order.service.exception.DownstreamServiceTimeoutException;
import com.resiliencelab.order.service.exception.PaymentServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "paymentFallback"
    )
    @Retry(name = "paymentService")
    public PaymentResponse processPayment(UUID orderId, BigDecimal amount) {

        PaymentRequest request = new PaymentRequest(orderId, amount);

        try {
            return restClient.post()
                    .uri(paymentServiceUrl + "/api/payments")
                    .body(request)
                    .retrieve()
                    .body(PaymentResponse.class);

        } catch (ResourceAccessException exception) {
            throw new DownstreamServiceTimeoutException(
                    "Payment service request timed out",
                    exception
            );
        }
    }

    private PaymentResponse paymentFallback(
            UUID orderId,
            BigDecimal amount,
            Throwable throwable) {

        log.error(
                "Payment service unavailable for order: {}",
                orderId,
                throwable
        );

        if (throwable instanceof DownstreamServiceTimeoutException) {
            throw (DownstreamServiceTimeoutException) throwable;
        }

        throw new PaymentServiceUnavailableException(
                "Payment service temporarily unavailable",
                throwable
        );
    }
}