package com.ridelink.ride.controller;

import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.security.UserPrincipal;
import com.ridelink.ride.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PASSENGER')")
    public RideResponse create(
            @Valid @RequestBody CreateRideRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.createRide(request, principal);
    }

    @GetMapping("/{id}")
    public RideResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.getById(id, principal);
    }

    @GetMapping("/passenger/me")
    @PreAuthorize("hasRole('PASSENGER')")
    public List<RideResponse> passengerRides(@AuthenticationPrincipal UserPrincipal principal) {
        return rideService.getPassengerRides(principal);
    }

    @GetMapping("/driver/me")
    @PreAuthorize("hasRole('DRIVER')")
    public List<RideResponse> driverRides(@AuthenticationPrincipal UserPrincipal principal) {
        return rideService.getDriverRides(principal);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public RideResponse accept(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.acceptRide(id, principal);
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public RideResponse start(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.startRide(id, principal);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public RideResponse complete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.completeRide(id, principal);
    }

    @PatchMapping("/{id}/cancel")
    public RideResponse cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return rideService.cancelRide(id, principal);
    }
}
