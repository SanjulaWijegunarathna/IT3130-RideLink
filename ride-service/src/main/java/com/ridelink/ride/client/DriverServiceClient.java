package com.ridelink.ride.client;

import com.ridelink.ride.dto.DriverAssignResponse;
import com.ridelink.ride.exception.ApiException;
import com.ridelink.ride.security.AuthTokenHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Calls the driver service. Outbound requests forward the inbound Bearer token
 * when present; this demo reuses the authenticated user's JWT for internal calls.
 */
@Component
public class DriverServiceClient {

    private final RestClient restClient;

    public DriverServiceClient(RestClient.Builder restClientBuilder,
                               @Value("${ridelink.driver-service-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public DriverAssignResponse assignFirstAvailableDriver() {
        try {
            return restClient.post()
                    .uri("/api/drivers/internal/assign-first")
                    .headers(this::applyAuthHeader)
                    .retrieve()
                    .body(DriverAssignResponse.class);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new ApiException(HttpStatus.CONFLICT, "NO_DRIVER_AVAILABLE", "No driver is currently available");
        }
    }

    public void releaseDriver(Long driverId) {
        restClient.post()
                .uri("/api/drivers/{id}/release", driverId)
                .headers(this::applyAuthHeader)
                .retrieve()
                .toBodilessEntity();
    }

    private void applyAuthHeader(HttpHeaders headers) {
        String token = AuthTokenHolder.get();
        if (token != null) {
            headers.setBearerAuth(token);
        }
    }
}
