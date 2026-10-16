package com.ridelink.account.repository;

import com.ridelink.account.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
