package com.ridelink.driver.controller;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.security.UserPrincipal;
import com.ridelink.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.htp.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public DriverResponse register(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RegisterDriverRequest request) {
        return driverService.registerDriver(principal, request);
    }

    @GetMapping("/available")
    public List<DriverResponse> listAvailable() {
        return driverService.listAvailable();
    }

    @GetMapping("/{id}")
    public DriverResponse getById(@PathVariable Long id) {
        return driverService.getById(id);
    }

    @GetMapping("/by-account/{accountId}")
    public DriverResponse getByAccountId(@PathVariable Long accountId) {
        return driverService.getByAccountId(accountId);
    }

    @PatchMapping("/{id}/availability")
    public DriverResponse updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return driverService.updateAvailability(id, request.available(), principal);
    }

    @PatchMapping("/{id}/location")
    public DriverResponse updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return driverService.updateLocation(id, request, principal);
    }

    @PostMapping("/internal/assign-first")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN', 'PASSENGER')")
    public DriverResponse assignFirstAvailable() {
        return driverService.assignFirstAvailable();
    }

    @PostMapping("/{id}/release")
    public DriverResponse releaseDriver(@PathVariable Long id) {
        return driverService.releaseDriver(id);
    }
}
