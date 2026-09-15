package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDriverRequest(
        Long accountId,
        @NotBlank String vehicleNumber,
        @NotBlank String vehicleType,
        @NotBlank String serviceArea,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotBlank String fullName,
        String phone,
        Boolean available
) {}
