package com.ridelink.fare.controller;

import com.ridelink.fare.dto.PayRequest;
import com.ridelink.fare.dto.PaymentResponse;
import com.ridelink.fare.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody PayRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getByRideId(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.getByRideId(rideId));
    }

    @GetMapping("/receipt/{receiptNumber}")
    public ResponseEntity<PaymentResponse> getByReceipt(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(paymentService.getByReceipt(receiptNumber));
    }
}
