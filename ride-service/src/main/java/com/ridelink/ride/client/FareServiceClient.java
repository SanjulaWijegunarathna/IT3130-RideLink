package com.ridelink.ride.client;

import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.FareFinalRequest;
import com.ridelink.ride.dto.FareFinalResponse;
import com.ridelink.ride.dto.RideCompletedEvent;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.security.AuthTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Calls the fare/payment service. Outbound requests forward the inbound Bearer token
 * when present; this demo reuses the authenticated user's JWT for internal calls.
 */
@Component
public class FareServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FareServiceClient.class);

    private final RestClient restClient;
    private final Executor asyncExecutor = Executors.newCachedThreadPool();

    public FareServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${ridelink.fare-service-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public FareEstimateResponse estimateFare(CreateRideRequest rideRequest) {
        FareEstimateRequest request = new FareEstimateRequest(
                rideRequest.getPickup(),
                rideRequest.getDestination(),
                rideRequest.getPickupLat(),
                rideRequest.getPickupLng(),
                rideRequest.getDestLat(),
                rideRequest.getDestLng());
        return restClient.post()
                .uri("/api/fares/estimate")
                .headers(this::applyAuthHeader)
                .body(request)
                .retrieve()
                .body(FareEstimateResponse.class);
    }

    public FareFinalResponse calculateFinalFare(Ride ride) {
        FareFinalRequest request = new FareFinalRequest(
                ride.getId(),
                ride.getPickup(),
                ride.getDestination(),
                ride.getPickupLat(),
                ride.getPickupLng(),
                ride.getDestLat(),
                ride.getDestLng());
        return restClient.post()
                .uri("/api/fares/final")
                .headers(this::applyAuthHeader)
                .body(request)
                .retrieve()
                .body(FareFinalResponse.class);
    }

    public void publishRideCompletedEventAsync(Ride ride) {
        RideCompletedEvent event = new RideCompletedEvent(
                ride.getId(),
                ride.getPassengerAccountId(),
                ride.getDriverId(),
                ride.getDriverAccountId(),
                ride.getPickup(),
                ride.getDestination(),
                ride.getPickupLat(),
                ride.getPickupLng(),
                ride.getDestLat(),
                ride.getDestLng(),
                ride.getFinalFare());
        // Capture token before the request thread clears AuthTokenHolder.
        String token = AuthTokenHolder.get();

        CompletableFuture.runAsync(() -> {
            try {
                restClient.post()
                        .uri("/api/events/ride-completed")
                        .headers(headers -> {
                            if (token != null) {
                                headers.setBearerAuth(token);
                            }
                        })
                        .body(event)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ex) {
                log.warn("Failed to publish ride-completed event for ride {}: {}", ride.getId(), ex.getMessage());
            }
        }, asyncExecutor);
    }

    private void applyAuthHeader(HttpHeaders headers) {
        String token = AuthTokenHolder.get();
        if (token != null) {
            headers.setBearerAuth(token);
        }
    }
}
