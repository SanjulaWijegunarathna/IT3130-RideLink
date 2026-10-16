package com.ridelink.fare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "fares")
public class FareRecord {

    @Id
    private String id;
    private Long rideId;
    private String pickup;
    private String destination;
    private BigDecimal distanceKm;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal totalFare;
    private BigDecimal estimatedMinutes;
    private String currency;
    private String pricingRule;
    private String type; // ESTIMATE or FINAL
    private Instant createdAt = Instant.now();

    public FareRecord() {
    }

    public FareRecord(Long rideId, String pickup, String destination, BigDecimal distanceKm,
                      BigDecimal baseFare, BigDecimal perKmRate, BigDecimal totalFare,
                      BigDecimal estimatedMinutes, String currency, String pricingRule, String type) {
        this.rideId = rideId;
        this.pickup = pickup;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.totalFare = totalFare;
        this.estimatedMinutes = estimatedMinutes;
        this.currency = currency;
        this.pricingRule = pricingRule;
        this.type = type;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
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

    public String getPricingRule() {
        return pricingRule;
    }

    public void setPricingRule(String pricingRule) {
        this.pricingRule = pricingRule;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
