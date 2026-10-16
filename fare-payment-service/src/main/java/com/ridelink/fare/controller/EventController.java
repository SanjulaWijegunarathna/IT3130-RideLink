package com.ridelink.fare.controller;

import com.ridelink.fare.dto.RideCompletedEvent;
import com.ridelink.fare.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/events") // API call - This will send data to event
public class EventController {

    private final PaymentService paymentService;

    public EventController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Ride booking service will call this endpoint when a ride is completed

    @PostMapping("/ride-completed")
    // give response the event is received
    public ResponseEntity<Map<String, Object>> rideCompleted(@Valid @RequestBody RideCompletedEvent event) {
        paymentService.enqueueRideCompleted(event);// create queue to process payment

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "ACCEPTED",
                "message", "Ride completed event queued for async payment processing",
                "rideId", event.getRideId()));
    }
}
