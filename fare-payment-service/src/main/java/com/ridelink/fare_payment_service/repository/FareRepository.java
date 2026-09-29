package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.FareRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FareRepository extends MongoRepository<FareRecord, String> {
}