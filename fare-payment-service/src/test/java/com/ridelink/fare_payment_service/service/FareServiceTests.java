package com.ridelink.fare_payment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.entity.FareRecord;
import com.ridelink.fare_payment_service.repository.FareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FareServiceTests {

    private FareService fareService;

    @BeforeEach
    void setUp() {
        FareRepository fareRepository = mock(FareRepository.class);
        when(fareRepository.save(any(FareRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        fareService = new FareService(fareRepository);
    }

    @Test
    void estimateUsesTextDistanceWhenCoordinatesAreMissing() {
        FareRecord estimate = fareService.estimate(new FareRequest("SLIIT Malabe", "Colombo Fort", null,
                null, null, null, null));

        assertEquals(6.0, estimate.getDistanceKm());
        assertEquals(420.0, estimate.getEstimatedFare());
    }

    @Test
    void estimateUsesCoordinatesWhenProvided() {
        FareRecord estimate = fareService.estimate(new FareRequest("Malabe", "Fort", null,
                6.9147, 79.9729, 6.9271, 79.8612));

        assertEquals(150.0 + estimate.getDistanceKm() * 45.0, estimate.getEstimatedFare(), 0.01);
    }

    @Test
    void estimateRejectsPartialCoordinates() {
        FareRequest request = new FareRequest("Malabe", "Fort", null,
                6.9147, 79.9729, null, null);

        assertThrows(IllegalArgumentException.class, () -> fareService.estimate(request));
    }
}