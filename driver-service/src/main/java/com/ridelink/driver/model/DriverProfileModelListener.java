package com.ridelink.driver.model;

import com.ridelink.driver.service.SequenceGeneratorService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DriverProfileModelListener extends AbstractMongoEventListener<DriverProfile> {

    private final SequenceGeneratorService sequenceGenerator;

    public DriverProfileModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<DriverProfile> event) {
        DriverProfile profile = event.getSource();
        if (profile.getId() == null || profile.getId() < 1) {
            profile.setId(sequenceGenerator.generateSequence(DriverProfile.SEQUENCE_NAME));
        }
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(Instant.now());
        }
    }
}
