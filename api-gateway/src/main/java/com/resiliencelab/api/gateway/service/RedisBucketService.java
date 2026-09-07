package com.resiliencelab.api.gateway.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class RedisBucketService {

    private final ProxyManager<byte[]> proxyManager;

    public RedisBucketService(RedisClient redisClient) {
        this.proxyManager = Bucket4jLettuce
                .casBasedBuilder(redisClient)
                .build();
    }

    public Bucket resolveBucket(String key) {

        BucketConfiguration configuration =
                BucketConfiguration.builder()
                        .addLimit(
                                Bandwidth.simple(
                                        20,
                                        Duration.ofMinutes(1)
                                )
                        )
                        .build();

        byte[] redisKey = key.getBytes(StandardCharsets.UTF_8);

        return proxyManager.getProxy(
                redisKey,
                () -> configuration
        );
    }
}