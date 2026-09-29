package com.ridelink.fare_payment_service.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDatabaseInitializer {

    @Bean
    ApplicationRunner initializeFareCollections(MongoTemplate mongoTemplate) {
        return args -> {
            if (!mongoTemplate.collectionExists("fares")) {
                mongoTemplate.createCollection("fares");
            }
            if (!mongoTemplate.collectionExists("payments")) {
                mongoTemplate.createCollection("payments");
            }
        };
    }
}