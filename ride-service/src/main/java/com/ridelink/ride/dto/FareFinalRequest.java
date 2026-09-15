package com.ridelink.ride.dto;

public class FareFinalRequest {

    private Long rideId;
    private String pickup;
    private String destination;

    public FareFinalRequest() {
    }

    public FareFinalRequest(Long rideId, String pickup, String destination) {
        this.rideId = rideId;
        this.pickup = pickup;
        this.destination = destination;
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
}
