package com.resiliencelab.inventory.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaultService {

    private final StringRedisTemplate redisTemplate;

    public String getInventoryFault() {
        String mode = redisTemplate.opsForValue().get("fault:inventory");

        if (mode == null) {
            return "NORMAL";
        }

        return mode;
    }
}