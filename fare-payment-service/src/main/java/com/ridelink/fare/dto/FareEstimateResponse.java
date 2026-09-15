package com.ridelink.fare.dto;

import java.math.BigDecimal;

public class FareEstimateResponse {

    private BigDecimal distanceKm;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal estimatedFare;
    private BigDecimal estimatedMinutes;
    private String currency;
    private String ruleDescription;

    public FareEstimateResponse() {
    }

    public FareEstimateResponse(BigDecimal distanceKm, BigDecimal baseFare, BigDecimal perKmRate,
                                BigDecimal estimatedFare, BigDecimal estimatedMinutes,
                                String currency, String ruleDescription) {
        this.distanceKm = distanceKm;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.estimatedFare = estimatedFare;
        this.estimatedMinutes = estimatedMinutes;
        this.currency = currency;
        this.ruleDescription = ruleDescription;
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

    public BigDecimal getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(BigDecimal estimatedFare) {
        this.estimatedFare = estimatedFare;
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
