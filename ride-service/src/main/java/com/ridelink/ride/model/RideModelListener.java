package com.ridelink.ride.model;

import com.ridelink.ride.service.SequenceGeneratorService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RideModelListener extends AbstractMongoEventListener<Ride> {

    private final SequenceGeneratorService sequenceGenerator;

    public RideModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Ride> event) {
        Ride ride = event.getSource();
        if (ride.getId() == null || ride.getId() < 1) {
            ride.setId(sequenceGenerator.generateSequence(Ride.SEQUENCE_NAME));
        }
        if (ride.getCreatedAt() == null) {
            ride.setCreatedAt(Instant.now());
        }
        ride.setUpdatedAt(Instant.now());
    }
}
