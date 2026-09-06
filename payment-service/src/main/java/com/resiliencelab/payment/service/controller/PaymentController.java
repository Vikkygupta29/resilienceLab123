package com.resiliencelab.payment.service.controller;

import com.resiliencelab.payment.service.dto.PaymentRequest;
import com.resiliencelab.payment.service.dto.PaymentResponse;
import com.resiliencelab.payment.service.service.FaultService;
import com.resiliencelab.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final FaultService faultService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED )
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request){

        String faultMode = faultService.getPaymentFault();

        if ("LATENCY".equals(faultMode)) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if ("FAIL".equals(faultMode)) {
            throw new RuntimeException("Simulated payment failure");
        }

        if ("RATE_LIMITED".equals(faultMode)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }

        if ("TIMEOUT".equals(faultMode)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

           return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
