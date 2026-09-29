package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRideRequest {

    @NotBlank(message = "pickup is required")
    private String pickup;

    @NotBlank(message = "destination is required")
    private String destination;

    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;

    public boolean hasPartialCoordinates() {
        int present = 0;
        if (pickupLat != null) {
            present++;
        }
        if (pickupLng != null) {
            present++;
        }
        if (destLat != null) {
            present++;
        }
        if (destLng != null) {
            present++;
        }
        return present > 0 && present < 4;
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
