package com.resiliencelab.order.service.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaultService {

    private final StringRedisTemplate redisTemplate;

    public void setInventoryFault(String mode) {
        redisTemplate.opsForValue().set("fault:inventory", mode);
    }

    public String getInventoryFault() {
        return redisTemplate.opsForValue().get("fault:inventory");
    }

    public void clearInventoryFault() {
        redisTemplate.delete("fault:inventory");
    }

    public void setPaymentFault(String mode) {
        redisTemplate.opsForValue().set("fault:payment", mode);
    }

    public String getPaymentFault() {
        return redisTemplate.opsForValue().get("fault:payment");
    }

    public void clearPaymentFault() {
        redisTemplate.delete("fault:payment");
    }
}
