package com.resiliencelab.api.gateway.config;

import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4jConfig {

    @Bean
    public AsyncProxyManager<String> bucket4jProxyManager(
            StatefulRedisConnection<String, byte[]> redisConnection) {

        return Bucket4jLettuce
                .casBasedBuilder(redisConnection)
                .build()
                .asAsync();
    }
}