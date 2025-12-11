package com.controller;

import com.dto.AmountRequest;
import com.dto.CreateAccountRequest;
import com.model.Account;
import com.model.Transaction;
import com.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 I am testing AccountController as a plain class.
 I mock AccountService and call controller methods directly.
*/

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setup() {
        // nothing special for now; @InjectMocks handles wiring
    }

    // -------- createJson (POST /api/accounts) --------

    @Test
    @DisplayName("createJson should call service with name from body and return 201 + account")
    void createJson_shouldCreateAccountFromBody() {
        // arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Raj");

        Account created = new Account("RA1000", "Raj", 0L);
        when(accountService.createAccount("Raj")).thenReturn(created);

        // act
        ResponseEntity<Account> response = accountController.createJson(request);

        // assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created, response.getBody());
        assertEquals("/api/accounts/RA1000",
                response.getHeaders().getLocation().toString());
        verify(accountService, times(1)).createAccount("Raj");
    }

    // -------- createByName (POST /api/accounts/{name}) --------

    @Test
    @DisplayName("createByName should call service with path variable and return 201 + account")
    void createByName_shouldCreateAccountFromPath() {
        Account created = new Account("RA1001", "Ravi", 0L);
        when(accountService.createAccount("Ravi")).thenReturn(created);

        ResponseEntity<Account> response = accountController.createByName("Ravi");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created, response.getBody());
        assertEquals("/api/accounts/RA1001",
                response.getHeaders().getLocation().toString());
        verify(accountService, times(1)).createAccount("Ravi");
    }

    // -------- get (GET /api/accounts/{accountNumber}) --------

    @Test
    @DisplayName("get should return 200 + account from service")
    void get_shouldReturnAccount() {
        Account acc = new Account("RA1000", "Raj", 500L);
        when(accountService.getByAccountNumber("RA1000")).thenReturn(acc);

        ResponseEntity<Account> response = accountController.get("RA1000");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(acc, response.getBody());
        verify(accountService, times(1)).getByAccountNumber("RA1000");
    }

    // -------- delete (DELETE /api/accounts/{accountNumber}) --------

    @Test
    @DisplayName("delete should call service and return 204")
    void delete_shouldReturnNoContent() {
        ResponseEntity<Void> response = accountController.delete("RA1000");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService, times(1)).deleteByAccountNumber("RA1000");
    }

    // -------- deposit (POST /api/accounts/{accountNumber}/deposit) --------

    @Test
    @DisplayName("deposit with amount param should prefer amountParam over body")
    void deposit_withRequestParam_shouldUseAmountParam() {
        Transaction tx = new Transaction("acc-id", "DEPOSIT", 500L,
                Instant.now(), "deposit");
        when(accountService.deposit("RA1000", 500L)).thenReturn(tx);

        // body is null, amountParam is 500 -> ternary uses amountParam
        ResponseEntity<Transaction> response =
                accountController.deposit("RA1000", null, 500L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tx, response.getBody());
        verify(accountService, times(1)).deposit("RA1000", 500L);
    }

    @Test
    @DisplayName("deposit with body and null param should use body amount")
    void deposit_withBody_shouldUseBody() {
        Transaction tx = new Transaction("acc-id", "DEPOSIT", 700L,
                Instant.now(), "deposit");
        when(accountService.deposit("RA1000", 700L)).thenReturn(tx);

        AmountRequest body = new AmountRequest();
        body.setAmount(700L);

        // amountParam is null, body not null -> ternary uses body.getAmount()
        ResponseEntity<Transaction> response =
                accountController.deposit("RA1000", body, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tx, response.getBody());
        verify(accountService, times(1)).deposit("RA1000", 700L);
    }

    // -------- withdraw (POST /api/accounts/{accountNumber}/withdraw) --------

    @Test
    @DisplayName("withdraw should call service with resolved amount")
    void withdraw_withBody_shouldUseBody() {
        Transaction tx = new Transaction("acc-id", "WITHDRAW", 300L,
                Instant.now(), "withdraw");
        when(accountService.withdraw("RA1000", 300L)).thenReturn(tx);

        AmountRequest body = new AmountRequest();
        body.setAmount(300L);

        ResponseEntity<Transaction> response =
                accountController.withdraw("RA1000", body, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tx, response.getBody());
        verify(accountService, times(1)).withdraw("RA1000", 300L);
    }

    // -------- transfer (POST /api/accounts/{from}/transfer/{to}) --------

    @Test
    @DisplayName("transfer should call service with from, to and resolved amount")
    void transfer_withBody_shouldUseBodyAmount() {
        Transaction tx = new Transaction("from-id", "TRANSFER", 200L,
                Instant.now(), "transfer to RA1001");
        when(accountService.transfer("RA1000", "RA1001", 200L))
                .thenReturn(tx);

        AmountRequest body = new AmountRequest();
        body.setAmount(200L);

        ResponseEntity<Transaction> response =
                accountController.transfer("RA1000", "RA1001", body, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tx, response.getBody());
        verify(accountService, times(1))
                .transfer("RA1000", "RA1001", 200L);
    }

    // -------- transactions (GET /api/accounts/{accountNumber}/transactions) --------

    @Test
    @DisplayName("transactions should return list from service")
    void transactions_shouldReturnList() {
        Transaction t1 = new Transaction("acc-id", "DEPOSIT", 100L,
                Instant.now(), "one");
        Transaction t2 = new Transaction("acc-id", "WITHDRAW", 50L,
                Instant.now(), "two");

        when(accountService.getTransactions("RA1000"))
                .thenReturn(List.of(t1, t2));

        ResponseEntity<List<Transaction>> response =
                accountController.transactions("RA1000");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("DEPOSIT", response.getBody().get(0).getType());
        assertEquals("WITHDRAW", response.getBody().get(1).getType());
        verify(accountService, times(1)).getTransactions("RA1000");
    }

    // -------- close (PUT /api/accounts/{accountNumber}/close) --------

    @Test
    @DisplayName("close should call service and return updated account")
    void close_shouldReturnClosedAccount() {
        Account acc = new Account("RA1000", "Raj", 0L);
        when(accountService.closeAccount("RA1000")).thenReturn(acc);

        ResponseEntity<Account> response = accountController.close("RA1000");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(acc, response.getBody());
        verify(accountService, times(1)).closeAccount("RA1000");
    }
}
