package com.ridelink.ride.dto;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class RideResponse {

    private Long id;
    private Long passengerAccountId;
    private Long driverId;
    private Long driverAccountId;
    private String pickup;
    private String destination;
    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;
    private RideStatus status;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private Instant createdAt;
    private Instant updatedAt;

    public static RideResponse from(Ride ride) {
        RideResponse response = new RideResponse();
        response.id = ride.getId();
        response.passengerAccountId = ride.getPassengerAccountId();
        response.driverId = ride.getDriverId();
        response.driverAccountId = ride.getDriverAccountId();
        response.pickup = ride.getPickup();
        response.destination = ride.getDestination();
        response.pickupLat = ride.getPickupLat();
        response.pickupLng = ride.getPickupLng();
        response.destLat = ride.getDestLat();
        response.destLng = ride.getDestLng();
        response.status = ride.getStatus();
        response.estimatedFare = ride.getEstimatedFare();
        response.finalFare = ride.getFinalFare();
        response.createdAt = ride.getCreatedAt();
        response.updatedAt = ride.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getPassengerAccountId() {
        return passengerAccountId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public Long getDriverAccountId() {
        return driverAccountId;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDestination() {
        return destination;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public Double getDestLat() {
        return destLat;
    }

    public Double getDestLng() {
        return destLng;
    }

    public RideStatus getStatus() {
        return status;
    }

    public BigDecimal getEstimatedFare() {
        return estimatedFare;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
