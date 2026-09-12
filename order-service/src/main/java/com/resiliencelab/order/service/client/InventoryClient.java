package com.resiliencelab.order.service.client;

import com.resiliencelab.order.service.dto.client.InventoryResponse;
import com.resiliencelab.order.service.dto.client.ReserveInventoryRequest;
import com.resiliencelab.order.service.exception.DownstreamServiceTimeoutException;
import com.resiliencelab.order.service.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient restClient;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @CircuitBreaker(
            name = "inventoryService",
            fallbackMethod = "inventoryFallback"
    )
    @Retry(name = "inventoryService")
    @Bulkhead(
            name = "inventoryService",
            type = Bulkhead.Type.SEMAPHORE
    )
    @RateLimiter(
            name = "inventoryService",
            fallbackMethod = "rateLimiterFallback"
    )
    public InventoryResponse reserveInventory(String productId, int quantity) {

        ReserveInventoryRequest request =
                new ReserveInventoryRequest(quantity);

        try {
            return restClient.post()
                    .uri(
                            inventoryServiceUrl +
                                    "/api/inventory/{productId}/reservations",
                            productId
                    )
                    .body(request)
                    .retrieve()
                    .body(InventoryResponse.class);

        } catch (ResourceAccessException exception) {
            throw new DownstreamServiceTimeoutException(
                    "Inventory service request timed out",
                    exception
            );
        }
    }

    private InventoryResponse inventoryFallback(
            String productId,
            int quantity,
            Throwable throwable) {

        log.error(
                "Inventory service unavailable for product: {}",
                productId,
                throwable
        );

        if (throwable instanceof DownstreamServiceTimeoutException) {
            throw (DownstreamServiceTimeoutException) throwable;
        }

        throw new InventoryServiceUnavailableException(
                "Inventory service temporarily unavailable",
                throwable
        );
    }

    private InventoryResponse rateLimiterFallback(
            String productId,
            int quantity,
            Throwable throwable) {

        log.warn(
                "Rate limiter rejected inventory request for product: {}",
                productId,
                throwable
        );

        throw new InventoryServiceUnavailableException(
                "Inventory request rate limit exceeded",
                throwable
        );
    }
}