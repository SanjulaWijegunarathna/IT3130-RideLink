package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FareServiceClient;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.DriverAssignResponse;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FareServiceClient fareServiceClient;

    @InjectMocks
    private RideService rideService;

    private UserPrincipal passenger;
    private UserPrincipal driver;

    @BeforeEach
    void setUp() {
        passenger = new UserPrincipal(10L, "passenger@example.com", "PASSENGER", true);
        driver = new UserPrincipal(20L, "driver@example.com", "DRIVER", true);
    }

    @Test
    void createRide_savesAssignedRideWhenDriverAvailable() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup("A");
        request.setDestination("B");

        FareEstimateResponse estimate = new FareEstimateResponse();
        estimate.setEstimatedFare(new BigDecimal("25.50"));

        DriverAssignResponse assignment = new DriverAssignResponse();
        assignment.setDriverId(100L);
        assignment.setAccountId(20L);

        when(fareServiceClient.estimateFare("A", "B")).thenReturn(estimate);
        when(driverServiceClient.assignFirstAvailableDriver()).thenReturn(assignment);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            ride.setId(1L);
            return ride;
        });

        var response = rideService.createRide(request, passenger);

        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals(100L, response.getDriverId());
        assertEquals(new BigDecimal("25.50"), response.getEstimatedFare());
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void createRide_throwsNoDriverAvailableAndDoesNotSave() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup("A");
        request.setDestination("B");

        when(fareServiceClient.estimateFare("A", "B")).thenReturn(new FareEstimateResponse());
        when(driverServiceClient.assignFirstAvailableDriver())
                .thenThrow(new ApiException(HttpStatus.CONFLICT, "NO_DRIVER_AVAILABLE", "No driver is currently available"));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.createRide(request, passenger));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("NO_DRIVER_AVAILABLE", ex.getError());
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @ParameterizedTest
    @CsvSource({
            "ASSIGNED,ACCEPTED",
            "ASSIGNED,CANCELLED",
            "ACCEPTED,IN_PROGRESS",
            "ACCEPTED,CANCELLED",
            "IN_PROGRESS,COMPLETED",
            "IN_PROGRESS,CANCELLED",
            "REQUESTED,CANCELLED"
    })
    void transition_allowsValidTransitions(RideStatus from, RideStatus to) {
        Ride ride = new Ride();
        ride.setStatus(from);

        RideService.transition(ride, to);

        assertEquals(to, ride.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "ASSIGNED,IN_PROGRESS",
            "ACCEPTED,COMPLETED",
            "COMPLETED,CANCELLED",
            "CANCELLED,ASSIGNED",
            "IN_PROGRESS,ACCEPTED"
    })
    void transition_rejectsInvalidTransitions(RideStatus from, RideStatus to) {
        Ride ride = new Ride();
        ride.setStatus(from);

        ApiException ex = assertThrows(ApiException.class, () -> RideService.transition(ride, to));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("INVALID_RIDE_TRANSITION", ex.getError());
        assertEquals(from, ride.getStatus());
    }

    @Test
    void acceptRide_requiresAssignedDriver() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverAccountId(99L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.acceptRide(1L, driver));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void acceptRide_transitionsToAccepted() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverAccountId(20L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        var response = rideService.acceptRide(1L, driver);

        assertEquals(RideStatus.ACCEPTED, response.getStatus());
    }
}
