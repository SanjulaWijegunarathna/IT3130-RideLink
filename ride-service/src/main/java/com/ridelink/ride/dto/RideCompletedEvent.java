package com.ridelink.ride.dto;

import java.math.BigDecimal;

public class RideCompletedEvent {

    private Long rideId;
    private Long passengerAccountId;
    private Long driverId;
    private Long driverAccountId;
    private String pickup;
    private String destination;
    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;
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
            Double pickupLat,
            Double pickupLng,
            Double destLat,
            Double destLng,
            BigDecimal finalFare) {
        this.rideId = rideId;
        this.passengerAccountId = passengerAccountId;
        this.driverId = driverId;
        this.driverAccountId = driverAccountId;
        this.pickup = pickup;
        this.destination = destination;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.destLat = destLat;
        this.destLng = destLng;
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

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public Double getDestLat() {
        return destLat;
    }

    public void setDestLat(Double destLat) {
        this.destLat = destLat;
    }

    public Double getDestLng() {
        return destLng;
    }

    public void setDestLng(Double destLng) {
        this.destLng = destLng;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }
}
