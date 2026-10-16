package com.ridelink.fare.repository;

import com.ridelink.fare.model.FareRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FareRepository extends MongoRepository<FareRecord, String> {
}
