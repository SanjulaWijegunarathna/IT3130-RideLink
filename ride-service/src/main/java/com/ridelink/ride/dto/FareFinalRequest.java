package com.ridelink.ride.dto;

public class FareFinalRequest {

    private Long rideId;
    private String pickup;
    private String destination;
    private Double pickupLat;
    private Double pickupLng;
    private Double destLat;
    private Double destLng;

    public FareFinalRequest() {
    }

    public FareFinalRequest(Long rideId, String pickup, String destination,
                            Double pickupLat, Double pickupLng,
                            Double destLat, Double destLng) {
        this.rideId = rideId;
        this.pickup = pickup;
        this.destination = destination;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.destLat = destLat;
        this.destLng = destLng;
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
