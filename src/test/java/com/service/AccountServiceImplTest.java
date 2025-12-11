package com.service;

import com.exception.AccountNotFoundException;
import com.exception.InsufficientBalanceException;
import com.model.Account;
import com.model.Transaction;
import com.repository.AccountRepository;
import com.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*
 I have created this test class so that I can hit every branch and line in AccountServiceImpl.
 I am mocking the repositories so that I only focus on the business logic inside the service.
*/
class AccountServiceImplTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        // I have chosen to create fresh mocks before each test so that tests do not interfere with each other.
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new AccountServiceImpl(accountRepository, transactionRepository);
    }

    // ---------- createAccount tests ----------

    @Test
    void createAccount_success_firstTime_noCollision() {
        // I have chosen a normal name so that prefix calculation and sequence both run.
        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount("Raj Kumar");

        assertNotNull(created.getAccountNumber());
        assertEquals("Raj Kumar", created.getAccountHolderName());
        // I am verifying that repository.save was actually called so that I know the account is persisted.
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_handlesCollisionAndRetries() {
        // I have created an existing account so that the first generated accountNumber collides.
        Account existing = new Account("RAJ1000", "Existing", 0);

        // First call: number exists -> Optional.of(existing)
        // Second call: next number free -> Optional.empty()
        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(existing))   // first iteration
                .thenReturn(Optional.empty());      // second iteration

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount("Raj");

        assertTrue(created.getAccountNumber().startsWith("RAJ"));
        // I have chosen to verify that findByAccountNumber was called at least twice due to collision.
        verify(accountRepository, atLeast(2)).findByAccountNumber(anyString());
    }

    @Test
    void createAccount_nullName_throwsException() {
        // I have passed null so that the null-check branch is covered.
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.createAccount(null));
        assertEquals("Account holder name must not be null", ex.getMessage());
    }

    @Test
    void createAccount_blankName_throwsException() {
        // I have passed spaces so that the empty-after-trim branch is covered.
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.createAccount("   "));
        assertEquals("Account holder name must not be blank", ex.getMessage());
    }

    // ---------- getByAccountNumber tests ----------

    @Test
    void getByAccountNumber_success() {
        Account acc = new Account("RAJ1000", "Raj", 1000);
        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));

        Account found = service.getByAccountNumber("RAJ1000");

        assertEquals("Raj", found.getAccountHolderName());
    }

    @Test
    void getByAccountNumber_notFound_throws() {
        when(accountRepository.findByAccountNumber("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.getByAccountNumber("UNKNOWN"));
    }

    // ---------- deleteByAccountNumber tests ----------

    @Test
    void deleteByAccountNumber_deletesAccountAndTransactions() {
        Account acc = new Account("RAJ1000", "Raj", 0);
        acc.setId("acc-id");
        List<Transaction> txs = new ArrayList<>();
        txs.add(new Transaction("acc-id", "DEPOSIT", 100, Instant.now(), "deposit"));

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));
        when(transactionRepository.findByAccountId("acc-id"))
                .thenReturn(txs);

        service.deleteByAccountNumber("RAJ1000");

        // I am verifying that both transactions and account are deleted so that this branch is fully covered.
        verify(transactionRepository, times(1)).deleteAll(txs);
        verify(accountRepository, times(1)).deleteByAccountNumber("RAJ1000");
    }

    @Test
    void deleteByAccountNumber_notFound_throws() {
        when(accountRepository.findByAccountNumber("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.deleteByAccountNumber("UNKNOWN"));
    }

    // ---------- deposit tests ----------

    @Test
    void deposit_success_updatesBalanceAndCreatesTransaction() {
        Account acc = new Account("RAJ1000", "Raj", 1000);
        acc.setId("acc-id");

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = service.deposit("RAJ1000", 500);

        assertEquals(1500, acc.getBalance());
        assertEquals("DEPOSIT", tx.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("RAJ1000", 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("RAJ1000", -10));
    }

    // ---------- withdraw tests ----------

    @Test
    void withdraw_success_updatesBalanceAndCreatesTransaction() {
        Account acc = new Account("RAJ1000", "Raj", 1000);
        acc.setId("acc-id");

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = service.withdraw("RAJ1000", 400);

        assertEquals(600, acc.getBalance());
        assertEquals("WITHDRAW", tx.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_insufficientBalance_throws() {
        Account acc = new Account("RAJ1000", "Raj", 100);
        acc.setId("acc-id");

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));

        assertThrows(InsufficientBalanceException.class,
                () -> service.withdraw("RAJ1000", 500));
    }

    @Test
    void withdraw_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw("RAJ1000", 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw("RAJ1000", -1));
    }

    // ---------- transfer tests ----------

    @Test
    void transfer_success_movesMoneyAndCreatesTransaction() {
        Account from = new Account("RAJ1000", "Raj", 1000);
        from.setId("from-id");
        Account to = new Account("RAV1001", "Ravi", 500);
        to.setId("to-id");

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("RAV1001"))
                .thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = service.transfer("RAJ1000", "RAV1001", 300);

        assertEquals(700, from.getBalance());
        assertEquals(800, to.getBalance());
        assertEquals("TRANSFER", tx.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_sameAccount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("RAJ1000", "RAJ1000", 100));
    }

    @Test
    void transfer_insufficientBalance_throws() {
        Account from = new Account("RAJ1000", "Raj", 100);
        from.setId("from-id");
        Account to = new Account("RAV1001", "Ravi", 500);
        to.setId("to-id");

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("RAV1001"))
                .thenReturn(Optional.of(to));

        assertThrows(InsufficientBalanceException.class,
                () -> service.transfer("RAJ1000", "RAV1001", 300));
    }

    @Test
    void transfer_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("RAJ1000", "RAV1001", 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("RAJ1000", "RAV1001", -5));
    }

    // ---------- getTransactions tests ----------

    @Test
    void getTransactions_returnsList() {
        Account acc = new Account("RAJ1000", "Raj", 0);
        acc.setId("acc-id");

        List<Transaction> txs = List.of(
                new Transaction("acc-id", "DEPOSIT", 100, Instant.now(), "deposit")
        );

        when(accountRepository.findByAccountNumber("RAJ1000"))
                .thenReturn(Optional.of(acc));
        when(transactionRepository.findByAccountId("acc-id"))
                .thenReturn(txs);

        List<Transaction> result = service.getTransactions("RAJ1000");

        assertEquals(1, result.size());
        assertEquals("DEPOSIT", result.get(0).getType());
    }
}
