package com.ridelink.account.model;

import com.ridelink.account.service.SequenceGeneratorService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserAccountModelListener extends AbstractMongoEventListener<UserAccount> {

    private final SequenceGeneratorService sequenceGenerator;

    public UserAccountModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<UserAccount> event) {
        UserAccount account = event.getSource();
        if (account.getId() == null || account.getId() < 1) {
            account.setId(sequenceGenerator.generateSequence(UserAccount.SEQUENCE_NAME));
        }
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(Instant.now());
        }
        account.setUpdatedAt(Instant.now());
    }
}
