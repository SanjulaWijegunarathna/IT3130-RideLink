package com.ridelink.fare_payment_service.repository;

import java.util.Optional;
import com.ridelink.fare_payment_service.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FarePaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRideId(Long rideId);
}