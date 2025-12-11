package com.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testGettersAndSetters() {
        Account acc = new Account();

        // Test ID
        acc.setId("123");
        assertEquals("123", acc.getId());

        // Test Account Number
        acc.setAccountNumber("AC1000");
        assertEquals("AC1000", acc.getAccountNumber());

        // Test Holder Name
        acc.setAccountHolderName("Raj");
        assertEquals("Raj", acc.getAccountHolderName());

        // Test Balance
        acc.setBalance(500L);
        assertEquals(500L, acc.getBalance());

        // Test Status
        acc.setStatus("ACTIVE");
        assertEquals("ACTIVE", acc.getStatus());

        // Test CreatedAt
        LocalDateTime now = LocalDateTime.now();
        acc.setCreatedAt(now);
        assertEquals(now, acc.getCreatedAt());

        // Test Transactions
        List<Transaction> list = new ArrayList<>();
        Transaction t = new Transaction();
        list.add(t);
        acc.setTransactions(list);
        assertEquals(1, acc.getTransactions().size());
        assertSame(t, acc.getTransactions().get(0));
    }

}
