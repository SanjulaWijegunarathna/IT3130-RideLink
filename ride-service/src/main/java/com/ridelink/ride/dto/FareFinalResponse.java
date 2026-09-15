package com.ridelink.ride.dto;

import java.math.BigDecimal;

public class FareFinalResponse {

    private BigDecimal finalFare;

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }
}
