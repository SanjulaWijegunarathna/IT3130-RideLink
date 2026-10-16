package com.ridelink.ride.repository;

import com.ridelink.ride.model.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RideRepository extends MongoRepository<Ride, Long> {

    List<Ride> findByPassengerAccountIdOrderByCreatedAtDesc(Long passengerAccountId);

    List<Ride> findByDriverAccountIdOrderByCreatedAtDesc(Long driverAccountId);
}
