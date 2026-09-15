package com.ridelink.fare.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PayRequest {

    @NotNull(message = "rideId is required")
    private Long rideId;

    @NotNull(message = "passengerAccountId is required")
    private Long passengerAccountId;

    private BigDecimal amount;

    private Boolean simulateFailure;

    public PayRequest() {
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getPassengerAccountId() {
        return passengerAccountId;
    }

    public void setPassengerAccountId(Long passengerAccountId) {
        this.passengerAccountId = passengerAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getSimulateFailure() {
        return simulateFailure;
    }

    public void setSimulateFailure(Boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
