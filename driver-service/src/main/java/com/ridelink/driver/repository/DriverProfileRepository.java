package com.ridelink.driver.repository;

import com.ridelink.driver.model.DriverProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository extends MongoRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByAccountId(Long accountId);

    List<DriverProfile> findByAvailableTrue();

    Optional<DriverProfile> findFirstByAvailableTrueOrderByIdAsc();
}
