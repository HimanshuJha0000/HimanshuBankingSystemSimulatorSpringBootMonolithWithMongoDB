package com.service;
import com.model.Account; import com.model.Transaction;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
public interface AccountService {
    Account createAccount(@NotBlank(message = "Account holder name must not be blank")String holderName);
    Account getByAccountNumber(String accountNumber);
    Account closeAccount(String accountNumber);
    void deleteByAccountNumber(String accountNumber);
    Transaction deposit(String accountNumber, long amount);
    Transaction withdraw(String accountNumber, long amount);
    Transaction transfer(String fromAccountNumber, String toAccountNumber, long amount);
    List<Transaction> getTransactions(String accountNumber);
}
