package com.ridelink.ride.repository;

import com.ridelink.ride.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByPassengerAccountIdOrderByCreatedAtDesc(Long passengerAccountId);

    List<Ride> findByDriverAccountIdOrderByCreatedAtDesc(Long driverAccountId);
}
