package com.ridelink.fare.service;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.dto.FareEstimateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FareServiceTest {

    private FareService fareService;

    @BeforeEach
    void setUp() {
        fareService = new FareService();
    }

    @Test
    void estimateUsesStringLengthRuleWhenCoordinatesMissing() {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickup("Colombo Fort Station");
        request.setDestination("Mount Lavinia Beach");

        FareEstimateResponse response = fareService.estimate(request);

        // pickup len=20, dest len=19 -> max(2.0, abs(1)*0.8 + 20*0.15) = 3.8
        // fare = 150 + 3.8*45 = 321.00
        assertEquals(new BigDecimal("3.80"), response.getDistanceKm());
        assertEquals(new BigDecimal("150.00"), response.getBaseFare());
        assertEquals(new BigDecimal("45.00"), response.getPerKmRate());
        assertEquals(new BigDecimal("321.00"), response.getEstimatedFare());
        assertEquals(new BigDecimal("11.40"), response.getEstimatedMinutes());
        assertEquals("LKR", response.getCurrency());
        assertTrue(response.getRuleDescription().contains("String-length estimate"));
    }

    @Test
    void estimateUsesHaversineWhenCoordinatesProvided() {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickup("Colombo");
        request.setDestination("Kandy");
        request.setPickupLat(6.9271);
        request.setPickupLng(79.8612);
        request.setDestLat(7.2906);
        request.setDestLng(80.6337);

        FareEstimateResponse response = fareService.estimate(request);

        assertTrue(response.getDistanceKm().compareTo(new BigDecimal("80.00")) > 0);
        assertEquals(new BigDecimal("150.00"), response.getBaseFare());
        assertEquals(new BigDecimal("45.00"), response.getPerKmRate());
        assertTrue(response.getEstimatedFare().compareTo(new BigDecimal("150.00")) > 0);
        assertTrue(response.getRuleDescription().contains("Haversine"));
    }

    @Test
    void estimateAppliesMinimumDistanceOfOneKm() {
        double distance = fareService.haversineKm(6.9271, 79.8612, 6.9272, 79.8613);
        assertTrue(distance < 1.0);

        BigDecimal distanceKm = fareService.calculateDistanceKm(
                "A", "B", 6.9271, 79.8612, 6.9272, 79.8613);
        assertEquals(new BigDecimal("1.00"), distanceKm);
    }
}
