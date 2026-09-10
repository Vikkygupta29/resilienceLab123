package com.resiliencelab.inventory.service.controller;

import com.resiliencelab.inventory.service.dto.InventoryResponse;
import com.resiliencelab.inventory.service.dto.ReserveInventoryRequest;
import com.resiliencelab.inventory.service.service.FaultService;
import com.resiliencelab.inventory.service.service.InventoryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final FaultService faultService;
    private final Counter faultsInjectedCounter;

    public InventoryController(
            InventoryService inventoryService,
            FaultService faultService,
            MeterRegistry meterRegistry) {

        this.inventoryService = inventoryService;
        this.faultService = faultService;

        this.faultsInjectedCounter = Counter.builder("faults_injected_total")
                .description("Total number of injected inventory faults")
                .register(meterRegistry);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(productId)
        );
    }

    @PostMapping("/{productId}/reservations")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InventoryResponse> reserveInventory(
            @PathVariable String productId,
            @Valid @RequestBody ReserveInventoryRequest request) {

        String faultMode = faultService.getInventoryFault();

        if ("LATENCY".equals(faultMode)) {

            faultsInjectedCounter.increment();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if ("FAIL".equals(faultMode)) {

            faultsInjectedCounter.increment();

            throw new RuntimeException(
                    "Simulated inventory failure"
            );
        }

        if ("TIMEOUT".equals(faultMode)) {

            faultsInjectedCounter.increment();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if ("RATE_LIMITED".equals(faultMode)) {

            faultsInjectedCounter.increment();

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }

        return ResponseEntity.ok(
                inventoryService.reserveInventory(
                        productId,
                        request
                )
        );
    }
}