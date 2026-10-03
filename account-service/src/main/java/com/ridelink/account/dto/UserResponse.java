package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;

public record UserResponse(
                String id,
                String email,
                String fullName,
                String phone,
                Role role,
                AccountStatus status) {
}
