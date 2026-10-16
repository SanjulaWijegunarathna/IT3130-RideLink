package com.ridelink.fare.repository;

import com.ridelink.fare.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(Long rideId);

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    boolean existsByRideId(Long rideId);
}
// CRUD operation interface