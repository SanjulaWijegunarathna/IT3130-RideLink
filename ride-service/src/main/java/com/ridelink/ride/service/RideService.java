package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FareServiceClient;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.DriverAssignResponse;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FareFinalResponse;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class RideService {

    private static final Set<RideStatus> TERMINAL_STATUSES = EnumSet.of(RideStatus.COMPLETED, RideStatus.CANCELLED);

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FareServiceClient fareServiceClient;

    public RideService(RideRepository rideRepository,
                       DriverServiceClient driverServiceClient,
                       FareServiceClient fareServiceClient) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.fareServiceClient = fareServiceClient;
    }

    @Transactional
    public RideResponse createRide(CreateRideRequest request, UserPrincipal passenger) {
        requireRole(passenger, "PASSENGER");

        FareEstimateResponse estimate = null;
        try {
            estimate = fareServiceClient.estimateFare(request.getPickup(), request.getDestination());
        } catch (Exception ignored) {
            // Fare estimate is optional; proceed without it when the fare service is unavailable.
        }

        DriverAssignResponse assignment = driverServiceClient.assignFirstAvailableDriver();

        Ride ride = new Ride();
        ride.setPassengerAccountId(passenger.getId());
        ride.setPickup(request.getPickup());
        ride.setDestination(request.getDestination());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverId(assignment.getDriverId());
        ride.setDriverAccountId(assignment.getAccountId());
        if (estimate != null && estimate.getEstimatedFare() != null) {
            ride.setEstimatedFare(estimate.getEstimatedFare());
        }

        return RideResponse.from(rideRepository.save(ride));
    }

    @Transactional(readOnly = true)
    public RideResponse getById(Long id, UserPrincipal principal) {
        Ride ride = findRide(id);
        assertCanView(ride, principal);
        return RideResponse.from(ride);
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getPassengerRides(UserPrincipal passenger) {
        requireRole(passenger, "PASSENGER");
        return rideRepository.findByPassengerAccountIdOrderByCreatedAtDesc(passenger.getId()).stream()
                .map(RideResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getDriverRides(UserPrincipal driver) {
        requireRole(driver, "DRIVER");
        return rideRepository.findByDriverAccountIdOrderByCreatedAtDesc(driver.getId()).stream()
                .map(RideResponse::from)
                .toList();
    }

    @Transactional
    public RideResponse acceptRide(Long id, UserPrincipal driver) {
        requireRole(driver, "DRIVER");
        Ride ride = findRide(id);
        assertAssignedDriver(ride, driver);
        transition(ride, RideStatus.ACCEPTED);
        return RideResponse.from(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse startRide(Long id, UserPrincipal driver) {
        requireRole(driver, "DRIVER");
        Ride ride = findRide(id);
        assertAssignedDriver(ride, driver);
        transition(ride, RideStatus.IN_PROGRESS);
        return RideResponse.from(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse completeRide(Long id, UserPrincipal driver) {
        requireRole(driver, "DRIVER");
        Ride ride = findRide(id);
        assertAssignedDriver(ride, driver);
        transition(ride, RideStatus.COMPLETED);

        FareFinalResponse finalFare = fareServiceClient.calculateFinalFare(ride);
        if (finalFare != null && finalFare.getFinalFare() != null) {
            ride.setFinalFare(finalFare.getFinalFare());
        }

        Ride saved = rideRepository.save(ride);
        fareServiceClient.publishRideCompletedEventAsync(saved);
        if (saved.getDriverId() != null) {
            driverServiceClient.releaseDriver(saved.getDriverId());
        }
        return RideResponse.from(saved);
    }

    @Transactional
    public RideResponse cancelRide(Long id, UserPrincipal principal) {
        Ride ride = findRide(id);
        assertCanCancel(ride, principal);
        transition(ride, RideStatus.CANCELLED);
        Ride saved = rideRepository.save(ride);
        if (saved.getDriverId() != null) {
            driverServiceClient.releaseDriver(saved.getDriverId());
        }
        return RideResponse.from(saved);
    }

    static void transition(Ride ride, RideStatus targetStatus) {
        RideStatus current = ride.getStatus();
        if (TERMINAL_STATUSES.contains(current)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "INVALID_RIDE_TRANSITION",
                    "Ride in status " + current + " cannot transition to " + targetStatus);
        }

        boolean allowed = switch (current) {
            case REQUESTED -> targetStatus == RideStatus.ASSIGNED || targetStatus == RideStatus.CANCELLED;
            case ASSIGNED -> targetStatus == RideStatus.ACCEPTED || targetStatus == RideStatus.CANCELLED;
            case ACCEPTED -> targetStatus == RideStatus.IN_PROGRESS || targetStatus == RideStatus.CANCELLED;
            case IN_PROGRESS -> targetStatus == RideStatus.COMPLETED || targetStatus == RideStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "INVALID_RIDE_TRANSITION",
                    "Ride in status " + current + " cannot transition to " + targetStatus);
        }

        ride.setStatus(targetStatus);
    }

    private Ride findRide(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND", "Ride not found"));
    }

    private void requireRole(UserPrincipal principal, String role) {
        if (!role.equals(principal.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only " + role + " users may perform this action");
        }
    }

    private void assertCanView(Ride ride, UserPrincipal principal) {
        if ("ADMIN".equals(principal.getRole())) {
            return;
        }
        if (principal.getId().equals(ride.getPassengerAccountId())) {
            return;
        }
        if (principal.getId().equals(ride.getDriverAccountId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You are not allowed to view this ride");
    }

    private void assertAssignedDriver(Ride ride, UserPrincipal driver) {
        if (ride.getDriverAccountId() == null || !ride.getDriverAccountId().equals(driver.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You are not the assigned driver for this ride");
        }
    }

    private void assertCanCancel(Ride ride, UserPrincipal principal) {
        if ("ADMIN".equals(principal.getRole())) {
            return;
        }
        if (principal.getId().equals(ride.getPassengerAccountId())) {
            return;
        }
        if (principal.getId().equals(ride.getDriverAccountId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You are not allowed to cancel this ride");
    }
}
