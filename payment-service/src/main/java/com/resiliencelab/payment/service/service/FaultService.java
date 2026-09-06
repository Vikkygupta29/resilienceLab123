package com.resiliencelab.payment.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaultService {

    private final StringRedisTemplate redisTemplate;

    public String getPaymentFault() {
        String mode = redisTemplate.opsForValue().get("fault:payment");

        if (mode == null) {
            return "NORMAL";
        }

        return mode;
    }
}