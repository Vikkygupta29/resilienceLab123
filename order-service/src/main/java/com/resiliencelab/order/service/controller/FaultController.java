package com.resiliencelab.order.service.controller;


import com.resiliencelab.order.service.service.FaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/faults")
@RequiredArgsConstructor
public class FaultController {

    private final FaultService faultService;

    @PostMapping("/inventory")
    public ResponseEntity<String> setInventoryFault(
            @RequestParam String mode) {

        faultService.setInventoryFault(mode);

        return ResponseEntity.ok(
                "Inventory fault mode set to: " + mode
        );
    }

    @DeleteMapping("/inventory")
    public ResponseEntity<String> clearInventoryFault() {

        faultService.clearInventoryFault();

        return ResponseEntity.ok(
                "Inventory fault mode cleared"
        );
    }

    @GetMapping("/inventory")
    public ResponseEntity<String> getInventoryFault() {

        String mode = faultService.getInventoryFault();

        if (mode == null) {
            mode = "NORMAL";
        }

        return ResponseEntity.ok(mode);
    }

    @PostMapping("/payment")
    public ResponseEntity<String> setPaymentFault(@RequestParam String mode) {
        faultService.setPaymentFault(mode);
        return ResponseEntity.ok("Payment fault mode set to: " + mode);
    }

    @DeleteMapping("/payment")
    public ResponseEntity<String> clearPaymentFault() {
        faultService.clearPaymentFault();
        return ResponseEntity.ok("Payment fault mode cleared");
    }

    @GetMapping("/payment")
    public ResponseEntity<String> getPaymentFault() {
        String mode = faultService.getPaymentFault();

        if (mode == null) {
            mode = "NORMAL";
        }

        return ResponseEntity.ok(mode);
    }
}
