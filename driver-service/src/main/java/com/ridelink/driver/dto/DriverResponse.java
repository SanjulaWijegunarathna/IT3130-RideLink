package com.ridelink.driver.dto;

import java.time.Instant;

public record DriverResponse(
        Long id,
        Long accountId,
        String fullName,
        String phone,
        String vehicleNumber,
        String vehicleType,
        String serviceArea,
        Double latitude,
        Double longitude,
        boolean available,
        Instant createdAt
) {}
