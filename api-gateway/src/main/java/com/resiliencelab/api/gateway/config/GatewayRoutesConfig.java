package com.resiliencelab.api.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> orderRoute =
                route("order-service")
                        .POST("/api/orders", http())
                        .before(uri("http://localhost:8081"))
                        .filter(
                                rateLimit(config -> config
                                        .setCapacity(20)
                                        .setPeriod(Duration.ofMinutes(1))
                                        .setKeyResolver(request -> "demo-user")
                                )
                        )
                        .build();

        RouterFunction<ServerResponse> inventoryRoute =
                route("inventory-service")
                        .GET("/api/inventory/{productId}", http())
                        .before(uri("http://localhost:8083"))
                        .build();

        RouterFunction<ServerResponse> paymentRoute =
                route("payment-service")
                        .POST("/api/payments", http())
                        .before(uri("http://localhost:8082"))
                        .build();

        return orderRoute
                .and(inventoryRoute)
                .and(paymentRoute);
    }
}