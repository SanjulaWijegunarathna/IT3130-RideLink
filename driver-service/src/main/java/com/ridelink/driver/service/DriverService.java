package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.exception.ApiException;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final DriverProfileRepository repository;

    public DriverService(DriverProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DriverResponse registerDriver(UserPrincipal principal, RegisterDriverRequest request) {
        requireRole(principal, "DRIVER", "ADMIN");

        Long accountId = resolveAccountId(principal, request.accountId());
        if (repository.findByAccountId(accountId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "DRIVER_EXISTS",
                    "A driver profile already exists for this account");
        }

        DriverProfile profile = new DriverProfile();
        profile.setAccountId(accountId);
        profile.setFullName(request.fullName().trim());
        profile.setPhone(request.phone());
        profile.setVehicleNumber(request.vehicleNumber().trim());
        profile.setVehicleType(request.vehicleType().trim());
        profile.setServiceArea(request.serviceArea().trim());
        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        profile.setAvailable(Boolean.TRUE.equals(request.available()));

        return toResponse(repository.save(profile));
    }

    public DriverResponse getById(Long id) {
        return toResponse(find(id));
    }

    public DriverResponse getByAccountId(Long accountId) {
        return toResponse(findByAccountId(accountId));
    }

    public List<DriverResponse> listAvailable() {
        return repository.findByAvailableTrue().stream().map(this::toResponse).toList();
    }

    @Transactional
    public DriverResponse updateAvailability(Long id, boolean available, UserPrincipal principal) {
        DriverProfile profile = find(id);
        requireOwnerOrAdmin(profile, principal);
        profile.setAvailable(available);
        return toResponse(repository.save(profile));
    }

    @Transactional
    public DriverResponse updateLocation(Long id, LocationRequest request, UserPrincipal principal) {
        DriverProfile profile = find(id);
        requireOwnerOrAdmin(profile, principal);
        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        if (request.serviceArea() != null && !request.serviceArea().isBlank()) {
            profile.setServiceArea(request.serviceArea().trim());
        }
        return toResponse(repository.save(profile));
    }

    @Transactional
    public DriverResponse assignFirstAvailable() {
        DriverProfile profile = repository.findFirstByAvailableTrueOrderByIdAsc()
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "NO_DRIVER_AVAILABLE",
                        "No available drivers at this time"));
        profile.setAvailable(false);
        return toResponse(repository.save(profile));
    }

    @Transactional
    public DriverResponse releaseDriver(Long id) {
        DriverProfile profile = find(id);
        profile.setAvailable(true);
        return toResponse(repository.save(profile));
    }

    private Long resolveAccountId(UserPrincipal principal, Long requestedAccountId) {
        if ("ADMIN".equals(principal.getRole())) {
            if (requestedAccountId != null) {
                return requestedAccountId;
            }
            return principal.getId();
        }
        return principal.getId();
    }

    private void requireRole(UserPrincipal principal, String... roles) {
        for (String role : roles) {
            if (role.equals(principal.getRole())) {
                return;
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Only " + String.join(" or ", roles) + " can perform this action");
    }

    private void requireOwnerOrAdmin(DriverProfile profile, UserPrincipal principal) {
        boolean owner = profile.getAccountId().equals(principal.getId());
        boolean admin = "ADMIN".equals(principal.getRole());
        if (!owner && !admin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Only the driver owner or ADMIN can perform this action");
        }
    }

    private DriverProfile find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DRIVER_NOT_FOUND", "Driver not found"));
    }

    private DriverProfile findByAccountId(Long accountId) {
        return repository.findByAccountId(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DRIVER_NOT_FOUND", "Driver not found"));
    }

    private DriverResponse toResponse(DriverProfile profile) {
        return new DriverResponse(
                profile.getId(),
                profile.getAccountId(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getVehicleNumber(),
                profile.getVehicleType(),
                profile.getServiceArea(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isAvailable(),
                profile.getCreatedAt());
    }
}
