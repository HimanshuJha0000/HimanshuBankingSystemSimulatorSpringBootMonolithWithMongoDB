package com.repository;

import com.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
    void deleteByAccountNumber(String accountNumber);
    Optional<Account> findByAccountHolderName(String accountHolderName);
}
