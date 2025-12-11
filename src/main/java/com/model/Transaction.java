package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "transaction")
public class Transaction {
    @Id private String id;
    private String accountId;
    private String type;
    private long amount;
    private Instant timestamp;
    private String note;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    public Transaction() {}
    public Transaction(String accountId, String type, long amount, Instant timestamp, String note) {
        this.accountId = accountId; this.type = type; this.amount = amount; this.timestamp = timestamp; this.note = note;
    }
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getAccountId() { return accountId; } public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public long getAmount() { return amount; } public void setAmount(long amount) { this.amount = amount; }
    public Instant getTimestamp() { return timestamp; } public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getNote() { return note; } public void setNote(String note) { this.note = note; }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }
    public Transaction(String accountId,
                       String type,
                       long amount,
                       Instant timestamp,
                       String note,
                       String sourceAccountNumber,
                       String destinationAccountNumber) {
        this(accountId, type, amount, timestamp, note);
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
    }
}
