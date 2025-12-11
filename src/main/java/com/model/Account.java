package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "account")
public class Account {
    @Id
    private String id;
    private String status ="Active";
    private String accountNumber;
    private String accountHolderName;
    private long balance;
    @DBRef(lazy = true)
    private List<Transaction> transactions;
    private LocalDateTime createdAt = LocalDateTime.now();


    public Account() { this.transactions = new ArrayList<>(); }
    public Account(String accountNumber, String accountHolderName, long balance) {
        this.accountNumber = accountNumber; this.accountHolderName = accountHolderName; this.balance = balance; this.transactions = new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; } public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolderName() { return accountHolderName; } public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public long getBalance() { return balance; } public void setBalance(long balance) { this.balance = balance; }
    public List<Transaction> getTransactions() { return transactions; } public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void addTransaction(Transaction tx) { if (this.transactions == null) this.transactions = new ArrayList<>(); this.transactions.add(tx); }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
