package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.FareServiceClient;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.DriverAssignResponse;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FareFinalResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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

        when(fareServiceClient.estimateFare(request)).thenReturn(estimate);
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

        when(fareServiceClient.estimateFare(request)).thenReturn(new FareEstimateResponse());
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

    @Test
    void createRide_stillAssignsDriverWhenFareEstimateFails() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup("A");
        request.setDestination("B");

        DriverAssignResponse assignment = new DriverAssignResponse();
        assignment.setDriverId(100L);
        assignment.setAccountId(20L);

        when(fareServiceClient.estimateFare(request)).thenThrow(new RuntimeException("fare down"));
        when(driverServiceClient.assignFirstAvailableDriver()).thenReturn(assignment);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = rideService.createRide(request, passenger);

        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertNull(response.getEstimatedFare());
    }

    @Test
    void createRide_rejectsDriverRole() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup("A");
        request.setDestination("B");

        ApiException ex = assertThrows(ApiException.class, () -> rideService.createRide(request, driver));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getError());
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverServiceClient, never()).assignFirstAvailableDriver();
    }

    @Test
    void createRide_rejectsPartialCoordinates() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup("SLIIT Malabe");
        request.setDestination("Colombo Fort");
        request.setPickupLat(6.9147);

        ApiException ex = assertThrows(ApiException.class, () -> rideService.createRide(request, passenger));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INCOMPLETE_COORDINATES", ex.getError());
        verify(driverServiceClient, never()).assignFirstAvailableDriver();
    }

    @Test
    void acceptRide_rejectsPassenger() {
        ApiException ex = assertThrows(ApiException.class, () -> rideService.acceptRide(1L, passenger));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(rideRepository, never()).findById(1L);
    }

    @Test
    void startRide_movesAcceptedRideToInProgress() {
        Ride ride = assignedRide(RideStatus.ACCEPTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        var response = rideService.startRide(1L, driver);

        assertEquals(RideStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void completeRide_setsFinalFareReleasesDriverThenPublishesEvent() {
        Ride ride = assignedRide(RideStatus.IN_PROGRESS);
        ride.setDriverId(100L);

        FareFinalResponse finalFare = new FareFinalResponse();
        finalFare.setFinalFare(new BigDecimal("330.00"));

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(fareServiceClient.calculateFinalFare(ride)).thenReturn(finalFare);
        when(rideRepository.save(ride)).thenReturn(ride);

        var response = rideService.completeRide(1L, driver);

        assertEquals(RideStatus.COMPLETED, response.getStatus());
        assertEquals(new BigDecimal("330.00"), response.getFinalFare());

        var order = inOrder(driverServiceClient, fareServiceClient);
        order.verify(driverServiceClient).releaseDriver(100L);
        order.verify(fareServiceClient).publishRideCompletedEventAsync(ride);
    }

    @Test
    void completeRide_rejectsSkipFromAssigned() {
        Ride ride = assignedRide(RideStatus.ASSIGNED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.completeRide(1L, driver));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("INVALID_RIDE_TRANSITION", ex.getError());
        verify(driverServiceClient, never()).releaseDriver(100L);
        verify(fareServiceClient, never()).publishRideCompletedEventAsync(any(Ride.class));
    }

    @Test
    void cancelRide_byPassengerReleasesDriver() {
        Ride ride = assignedRide(RideStatus.ASSIGNED);
        ride.setPassengerAccountId(10L);
        ride.setDriverId(100L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        var response = rideService.cancelRide(1L, passenger);

        assertEquals(RideStatus.CANCELLED, response.getStatus());
        verify(driverServiceClient).releaseDriver(100L);
    }

    @Test
    void cancelRide_rejectsUnrelatedPassenger() {
        Ride ride = assignedRide(RideStatus.ASSIGNED);
        ride.setPassengerAccountId(10L);
        UserPrincipal stranger = new UserPrincipal(77L, "other@example.com", "PASSENGER", true);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.cancelRide(1L, stranger));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(driverServiceClient, never()).releaseDriver(100L);
    }

    @Test
    void getById_rejectsUnrelatedUser() {
        Ride ride = assignedRide(RideStatus.ASSIGNED);
        ride.setPassengerAccountId(10L);
        UserPrincipal stranger = new UserPrincipal(77L, "other@example.com", "PASSENGER", true);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.getById(1L, stranger));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    private Ride assignedRide(RideStatus status) {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setStatus(status);
        ride.setDriverAccountId(20L);
        ride.setDriverId(100L);
        return ride;
    }
}
