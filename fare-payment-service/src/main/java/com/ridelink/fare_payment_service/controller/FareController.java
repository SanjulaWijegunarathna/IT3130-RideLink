package com.ridelink.fare_payment_service.controller;

import java.util.List;
import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.entity.FareRecord;
import com.ridelink.fare_payment_service.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/fares", "/api/v1/fares-payments"})
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @ResponseStatus(HttpStatus.CREATED)
    public FareRecord estimate(@Valid @RequestBody FareRequest request) {
        return fareService.estimate(request);
    }

    @PostMapping("/final")
    @ResponseStatus(HttpStatus.CREATED)
    public FareRecord finalFare(@Valid @RequestBody FareRequest request) {
        return fareService.finalFare(request);
    }

    @GetMapping
    public List<FareRecord> findAll() {
        return fareService.findAll();
    }
}