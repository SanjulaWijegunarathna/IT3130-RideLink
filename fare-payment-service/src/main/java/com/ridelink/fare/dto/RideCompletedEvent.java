package com.ridelink.fare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RideCompletedEvent {

    @NotNull(message = "rideId is required")
    private Long rideId;

    @NotNull(message = "passengerAccountId is required")
    private Long passengerAccountId;

    @NotBlank(message = "pickup is required")
    private String pickup;

    @NotBlank(message = "destination is required")
    private String destination;

    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;

    public RideCompletedEvent() {
    }

    public RideCompletedEvent(Long rideId, Long passengerAccountId, String pickup, String destination) {
        this.rideId = rideId;
        this.passengerAccountId = passengerAccountId;
        this.pickup = pickup;
        this.destination = destination;
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
}
