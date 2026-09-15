package com.ridelink.ride.dto;

import java.math.BigDecimal;

public class RideCompletedEvent {

    private Long rideId;
    private Long passengerAccountId;
    private Long driverId;
    private Long driverAccountId;
    private String pickup;
    private String destination;
    private BigDecimal finalFare;

    public RideCompletedEvent() {
    }

    public RideCompletedEvent(
            Long rideId,
            Long passengerAccountId,
            Long driverId,
            Long driverAccountId,
            String pickup,
            String destination,
            BigDecimal finalFare) {
        this.rideId = rideId;
        this.passengerAccountId = passengerAccountId;
        this.driverId = driverId;
        this.driverAccountId = driverAccountId;
        this.pickup = pickup;
        this.destination = destination;
        this.finalFare = finalFare;
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

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getDriverAccountId() {
        return driverAccountId;
    }

    public void setDriverAccountId(Long driverAccountId) {
        this.driverAccountId = driverAccountId;
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

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }
}
