package com.ridelink.fare.controller;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.dto.FareEstimateResponse;
import com.ridelink.fare.dto.FinalFareRequest;
import com.ridelink.fare.dto.FinalFareResponse;
import com.ridelink.fare.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    // EstimateFareController
    @PostMapping("/estimate")
    // Map incoming request body to FareEstimateRequest object (@Valid @RequestBody
    // FareEstimateRequest request)
    public ResponseEntity<FareEstimateResponse> estimate(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(fareService.estimate(request));
    }

    // Calculate and return the final fare for a completed ride
    @PostMapping("/final")
    public ResponseEntity<FinalFareResponse> finalFare(@Valid @RequestBody FinalFareRequest request) {
        return ResponseEntity.ok(fareService.calculateFinalFare(request));
    }
}
