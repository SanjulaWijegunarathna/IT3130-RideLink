package com.ridelink.fare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FarePaymentServiceApplication component.
 * FarePaymentServiceApplication supports the core runtime behavior of the fare-payment service.
 *
 * This file is part of the RideLink fare-payment service and documents the
 * responsibility of the code defined in this class or helper.
 */
@SpringBootApplication
@EnableScheduling
public class FarePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarePaymentServiceApplication.class, args);
    }
}

