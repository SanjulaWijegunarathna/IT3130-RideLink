package com.ridelink.fare.service;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.dto.FareEstimateResponse;
import com.ridelink.fare.dto.FinalFareRequest;
import com.ridelink.fare.dto.FinalFareResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fare calculation rules (RideLink demo):
 * <ul>
 *   <li>baseFare = 150.00 LKR</li>
 *   <li>perKm = 45.00 LKR</li>
 *   <li>distanceKm = max(1.0, computed distance)</li>
 *   <li>If all four coordinates are provided, distance uses the Haversine formula.</li>
 *   <li>Otherwise: distanceKm = max(2.0, abs(pickup.length() - destination.length()) * 0.8
 *       + pickup.length() * 0.15), capped at 50 km.</li>
 *   <li>fare = baseFare + (distanceKm * perKm), rounded to 2 decimal places.</li>
 *   <li>estimatedMinutes = distanceKm * 3 (rough urban average).</li>
 * </ul>
 */
@Service
public class FareService {

    public static final BigDecimal BASE_FARE = new BigDecimal("150.00");
    public static final BigDecimal PER_KM_RATE = new BigDecimal("45.00");
    private static final BigDecimal MIN_DISTANCE_KM = new BigDecimal("1.0");
    private static final BigDecimal MIN_STRING_DISTANCE_KM = new BigDecimal("2.0");
    private static final BigDecimal MAX_DISTANCE_KM = new BigDecimal("50.0");
    private static final BigDecimal MINUTES_PER_KM = new BigDecimal("3");
    private static final String CURRENCY = "LKR";

    private static final String HAVERSINE_RULE =
            "Haversine distance from coordinates; fare = 150.00 + (distanceKm * 45.00) LKR";
    private static final String STRING_RULE =
            "String-length estimate: max(2.0, abs(pickupLen - destLen)*0.8 + pickupLen*0.15), "
                    + "capped at 50 km; fare = 150.00 + (distanceKm * 45.00) LKR";

    public FareEstimateResponse estimate(FareEstimateRequest request) {
        BigDecimal distanceKm = calculateDistanceKm(
                request.getPickup(),
                request.getDestination(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDestLat(),
                request.getDestLng());

        String ruleDescription = hasAllCoordinates(
                request.getPickupLat(), request.getPickupLng(),
                request.getDestLat(), request.getDestLng())
                ? HAVERSINE_RULE
                : STRING_RULE;

        return buildEstimateResponse(distanceKm, ruleDescription);
    }

    public FinalFareResponse calculateFinalFare(FinalFareRequest request) {
        BigDecimal distanceKm = calculateDistanceKm(
                request.getPickup(),
                request.getDestination(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDestLat(),
                request.getDestLng());

        String ruleDescription = hasAllCoordinates(
                request.getPickupLat(), request.getPickupLng(),
                request.getDestLat(), request.getDestLng())
                ? HAVERSINE_RULE
                : STRING_RULE;

        FareEstimateResponse estimate = buildEstimateResponse(distanceKm, ruleDescription);

        return new FinalFareResponse(
                request.getRideId(),
                estimate.getDistanceKm(),
                estimate.getBaseFare(),
                estimate.getPerKmRate(),
                estimate.getEstimatedFare(),
                estimate.getEstimatedMinutes(),
                estimate.getCurrency(),
                ruleDescription);
    }

    private FareEstimateResponse buildEstimateResponse(BigDecimal distanceKm, String ruleDescription) {
        BigDecimal fare = BASE_FARE.add(distanceKm.multiply(PER_KM_RATE)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedMinutes = distanceKm.multiply(MINUTES_PER_KM).setScale(2, RoundingMode.HALF_UP);

        return new FareEstimateResponse(
                distanceKm,
                BASE_FARE,
                PER_KM_RATE,
                fare,
                estimatedMinutes,
                CURRENCY,
                ruleDescription);
    }

    BigDecimal calculateDistanceKm(String pickup, String destination,
                                   Double pickupLat, Double pickupLng,
                                   Double destLat, Double destLng) {
        double rawDistance;
        if (hasAllCoordinates(pickupLat, pickupLng, destLat, destLng)) {
            rawDistance = haversineKm(pickupLat, pickupLng, destLat, destLng);
            rawDistance = Math.max(MIN_DISTANCE_KM.doubleValue(), rawDistance);
        } else {
            rawDistance = estimateDistanceFromStrings(pickup, destination);
        }
        return BigDecimal.valueOf(rawDistance).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasAllCoordinates(Double pickupLat, Double pickupLng, Double destLat, Double destLng) {
        return pickupLat != null && pickupLng != null && destLat != null && destLng != null;
    }

    /**
     * Great-circle distance between two WGS-84 points, in kilometres.
     */
    double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    double estimateDistanceFromStrings(String pickup, String destination) {
        double pickupLen = pickup.length();
        double destLen = destination.length();
        double distance = Math.abs(pickupLen - destLen) * 0.8 + pickupLen * 0.15;
        distance = Math.max(MIN_STRING_DISTANCE_KM.doubleValue(), distance);
        distance = Math.min(MAX_DISTANCE_KM.doubleValue(), distance);
        return Math.max(MIN_DISTANCE_KM.doubleValue(), distance);
    }
}
