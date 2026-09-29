package com.ridelink.fare_payment_service.service;

import java.time.Instant;
import java.util.List;
import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.entity.FareRecord;
import com.ridelink.fare_payment_service.repository.FareRepository;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    private static final double BASE_FARE = 150.0;
    private static final double RATE_PER_KM = 45.0;
    private final FareRepository fareRepository;

    public FareService(FareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    public FareRecord estimate(FareRequest request) {
        return calculateAndSave(request, false);
    }

    public FareRecord finalFare(FareRequest request) {
        if (request.rideId() == null || request.rideId() <= 0) {
            throw new IllegalArgumentException("rideId must be a positive number");
        }
        return calculateAndSave(request, true);
    }

    public List<FareRecord> findAll() {
        return fareRepository.findAll();
    }

    private FareRecord calculateAndSave(FareRequest request, boolean isFinal) {
        double distanceKm = roundMoney(calculateDistance(request));
        double amount = roundMoney(BASE_FARE + distanceKm * RATE_PER_KM);

        FareRecord fare = new FareRecord();
        fare.setRideId(request.rideId());
        fare.setPickup(request.pickup().trim());
        fare.setDestination(request.destination().trim());
        fare.setDistanceKm(distanceKm);
        fare.setEstimatedFare(amount);
        fare.setFinalFare(isFinal ? amount : null);
        fare.setRuleDescription("LKR 150 base fare + LKR 45 per km");
        fare.setCreatedAt(Instant.now());
        return fareRepository.save(fare);
    }

    private double calculateDistance(FareRequest request) {
        Double pickupLatitude = request.pickupLatitude();
        Double pickupLongitude = request.pickupLongitude();
        Double destinationLatitude = request.destinationLatitude();
        Double destinationLongitude = request.destinationLongitude();
        boolean hasAnyCoordinate = pickupLatitude != null || pickupLongitude != null
                || destinationLatitude != null || destinationLongitude != null;
        boolean hasAllCoordinates = pickupLatitude != null && pickupLongitude != null
                && destinationLatitude != null && destinationLongitude != null;

        if (hasAnyCoordinate && !hasAllCoordinates) {
            throw new IllegalArgumentException("Provide all four coordinates or none");
        }
        if (!hasAnyCoordinate) {
            return Math.max(2.0, Math.min(50.0,
                    (request.pickup().trim().length() + request.destination().trim().length()) / 4.0));
        }
        validateCoordinate(pickupLatitude, -90, 90, "pickupLatitude");
        validateCoordinate(destinationLatitude, -90, 90, "destinationLatitude");
        validateCoordinate(pickupLongitude, -180, 180, "pickupLongitude");
        validateCoordinate(destinationLongitude, -180, 180, "destinationLongitude");

        double latitudeDifference = Math.toRadians(destinationLatitude - pickupLatitude);
        double longitudeDifference = Math.toRadians(destinationLongitude - pickupLongitude);
        double haversine = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(Math.toRadians(pickupLatitude)) * Math.cos(Math.toRadians(destinationLatitude))
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        haversine = Math.min(1.0, Math.max(0.0, haversine));
        return 6371.0 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private void validateCoordinate(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }

    private double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}