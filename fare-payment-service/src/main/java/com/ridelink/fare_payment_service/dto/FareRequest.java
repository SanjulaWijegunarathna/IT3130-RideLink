package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;

public record FareRequest(
        @NotBlank String pickup,
        @NotBlank String destination,
        Long rideId,
        Double pickupLatitude,
        Double pickupLongitude,
        Double destinationLatitude,
        Double destinationLongitude) {
}