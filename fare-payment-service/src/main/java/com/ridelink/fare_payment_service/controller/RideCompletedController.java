package com.ridelink.fare_payment_service.controller;

import java.util.Map;
import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RideCompletedController {

    private final PaymentService paymentService;

    public RideCompletedController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/events/ride-completed")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> rideCompleted(@Valid @RequestBody PaymentRequest request) {
        paymentService.processRideCompleted(request);
        return Map.of("message", "Ride completion accepted for payment processing");
    }
}