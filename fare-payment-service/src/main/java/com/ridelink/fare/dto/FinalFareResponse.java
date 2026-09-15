package com.ridelink.fare.dto;

import java.math.BigDecimal;

public class FinalFareResponse {

    private Long rideId;
    private BigDecimal distanceKm;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal finalFare;
    private BigDecimal estimatedMinutes;
    private String currency;
    private String ruleDescription;

    public FinalFareResponse() {
    }

    public FinalFareResponse(Long rideId, BigDecimal distanceKm, BigDecimal baseFare, BigDecimal perKmRate,
                             BigDecimal finalFare, BigDecimal estimatedMinutes,
                             String currency, String ruleDescription) {
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.finalFare = finalFare;
        this.estimatedMinutes = estimatedMinutes;
        this.currency = currency;
        this.ruleDescription = ruleDescription;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getPerKmRate() {
        return perKmRate;
    }

    public void setPerKmRate(BigDecimal perKmRate) {
        this.perKmRate = perKmRate;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }

    public BigDecimal getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(BigDecimal estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }
}
