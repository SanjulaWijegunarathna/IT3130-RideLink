package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps driver-service {@code DriverResponse} JSON ({@code id}, {@code accountId}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverAssignResponse {

    private Long id;
    private Long accountId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Convenience alias used by RideService for the driver profile id. */
    public Long getDriverId() {
        return id;
    }

    public void setDriverId(Long driverId) {
        this.id = driverId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
