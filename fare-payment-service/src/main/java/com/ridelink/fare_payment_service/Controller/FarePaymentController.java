package com.ridelink.fare_payment_service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fares-payments")
public class FarePaymentController {

    @PostMapping("/estimate")
    public ResponseEntity<Map<String, Object>> estimateFare(@RequestBody Map<String, Object> request) {
        Double distanceKm = Double.valueOf(request.get("distanceKm").toString());
        Integer durationMinutes = Integer.valueOf(request.get("durationMinutes").toString());
        String vehicleType = (String) request.get("vehicleType");

        // Base rate calculation logic
        double baseFare = 100.0;
        double distanceRate = 80.0; // Per Km
        double durationRate = 5.0;  // Per Minute

        double totalFare = baseFare + (distanceKm * distanceRate) + (durationMinutes * durationRate);

        if ("SEDAN".equalsIgnoreCase(vehicleType)) {
            totalFare *= 1.2; // 20% extra for Sedan
        }

        Map<String, Object> response = new HashMap<>();
        response.put("distanceKm", distanceKm);
        response.put("durationMinutes", durationMinutes);
        response.put("vehicleType", vehicleType);
        response.put("estimatedFare", totalFare);

        return ResponseEntity.ok(response);
    }
}