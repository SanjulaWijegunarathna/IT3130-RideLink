package com.ridelink.fare_payment_service.dto;

import com.ridelink.fare_payment_service.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotNull @Positive Long rideId,
        Long passengerAccountId,
        @NotNull @Positive Double amount,
        PaymentMethod paymentMethod,
        Boolean simulateFailure) {
}