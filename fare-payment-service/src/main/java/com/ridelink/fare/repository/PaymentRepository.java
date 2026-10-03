package com.ridelink.fare.repository;

import com.ridelink.fare.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    boolean existsByRideId(Long rideId);
}
