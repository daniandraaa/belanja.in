package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.PaymentDetailDto;
import com.ecommerce.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/validate")
    public ResponseEntity<?> validatePayment(@Valid @RequestBody PaymentDetailDto paymentDetailDto) {
        try {
            paymentService.validatePaymentDetail(paymentDetailDto);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Payment details are valid",
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }
}
