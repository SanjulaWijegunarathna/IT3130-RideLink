package com.ridelink.driver.service;

import com.ridelink.driver.exception.ApiException;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.repository.DriverProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverProfileRepository repository;

    @InjectMocks
    private DriverService driverService;

    @Test
    void assignFirstAvailable_returnsDriverAndMarksUnavailable() {
        DriverProfile profile = new DriverProfile();
        profile.setId(1L);
        profile.setAccountId(10L);
        profile.setFullName("Jane Driver");
        profile.setPhone("555-0100");
        profile.setVehicleNumber("ABC-123");
        profile.setVehicleType("SEDAN");
        profile.setServiceArea("Downtown");
        profile.setLatitude(12.34);
        profile.setLongitude(56.78);
        profile.setAvailable(true);

        when(repository.findFirstByAvailableTrueOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(repository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = driverService.assignFirstAvailable();

        assertEquals(1L, response.id());
        assertFalse(response.available());
        verify(repository).save(profile);
        assertFalse(profile.isAvailable());
    }

    @Test
    void assignFirstAvailable_whenNoDriverAvailable_throwsConflict() {
        when(repository.findFirstByAvailableTrueOrderByIdAsc()).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, driverService::assignFirstAvailable);

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("NO_DRIVER_AVAILABLE", ex.getError());
        verify(repository, never()).save(any());
    }
}
