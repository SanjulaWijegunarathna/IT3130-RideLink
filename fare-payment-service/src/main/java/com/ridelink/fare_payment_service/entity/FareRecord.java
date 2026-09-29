package com.ridelink.fare_payment_service.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fares")
public class FareRecord {

    @Id
    private String id;
    private Long rideId;
    private String pickup;
    private String destination;
    private double distanceKm;
    private double estimatedFare;
    private Double finalFare;
    private String ruleDescription;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public Double getFinalFare() {
        return finalFare;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public void setFinalFare(Double finalFare) {
        this.finalFare = finalFare;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}