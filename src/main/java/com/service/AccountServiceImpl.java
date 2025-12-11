package com.service;
import com.exception.AccountNotFoundException; import com.exception.InsufficientBalanceException;
import com.model.Account; import com.model.Transaction; import com.repository.AccountRepository; import com.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.stereotype.Service; import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank; import java.time.Instant; import java.util.List;import java.util.concurrent.atomic.AtomicLong;


@Service
@Validated
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountRepository accountRepository; private final TransactionRepository transactionRepository; private static final AtomicLong sequence = new AtomicLong(1000);
    public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository; this.transactionRepository = transactionRepository;
    }

    @Override
    public Account createAccount( String holderName) {
        // defensive trim & normalize spaces (but keep original casing for holderName stored)
        if (holderName == null) {
            throw new IllegalArgumentException("Account holder name must not be null");
        }
        String normalizedForPrefix = holderName.trim().replaceAll("\\s+", " ");
        if (normalizedForPrefix.isEmpty()) {
            throw new IllegalArgumentException("Account holder name must not be blank");
        }

        // compute uppercase 3-letter prefix from the holder name
        String prefix = normalizedForPrefix.toUpperCase().substring(0, Math.min(3, normalizedForPrefix.length()));

        // generate a unique account number using an atomic sequence.
        // also guard against (very unlikely) collision by checking repository.
        String accountNumber;
        do {
            long seq = sequence.getAndIncrement();        // thread-safe increment
            accountNumber = prefix + seq;                // e.g., RA1000, RA1001, ...
            // if repository already has this accountNumber loop and try next.
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        Account acc = new Account(accountNumber, holderName.trim(), 0L);
        Account saved = accountRepository.save(acc);
        log.info("Created account {}", saved.getAccountNumber());
        return saved;
}

@Override
public Account getByAccountNumber(String accountNumber) {
    return accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("No account: " + accountNumber));
}

@Override
public void deleteByAccountNumber(String accountNumber) {
    Account acc = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("No account: " + accountNumber));
            List<Transaction> txs = transactionRepository.findByAccountId(acc.getId());
    if (!txs.isEmpty()) { transactionRepository.deleteAll(txs); }
    accountRepository.deleteByAccountNumber(accountNumber);
    log.info("Deleted account {}", accountNumber);
}
    private void ensureActive(Account acc) {
        if (!"ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            throw new IllegalStateException("Account " + acc.getAccountNumber() + " is not active");
        }
    }

@Override
public Transaction deposit(String accountNumber, long amount) {
    if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
    Account acc = getByAccountNumber(accountNumber);
    ensureActive(acc);
    synchronized (acc) { acc.setBalance(acc.getBalance() + amount); accountRepository.save(acc); }
    Transaction tx = new Transaction(acc.getId(), "DEPOSIT", amount, Instant.now(), "deposit");
            Transaction saved = transactionRepository.save(tx);
    acc.addTransaction(saved); accountRepository.save(acc);
    log.info("Deposited {} to account {}", amount, accountNumber);
    return saved;
}

@Override
public Transaction withdraw( String accountNumber, long amount) {
    if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
    Account acc = getByAccountNumber(accountNumber);
    ensureActive(acc);
    synchronized (acc) {
        if (acc.getBalance() < amount) { throw new InsufficientBalanceException("Insufficient balance for " + accountNumber); }
                acc.setBalance(acc.getBalance() - amount); accountRepository.save(acc);
        }
        Transaction tx = new Transaction(acc.getId(), "WITHDRAW", amount, Instant.now(), "withdraw");
                Transaction saved = transactionRepository.save(tx); acc.addTransaction(saved); accountRepository.save(acc);
        log.info("Withdrew {} from account {}", amount, accountNumber);
        return saved;
    }

    @Override
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (fromAccountNumber.equals(toAccountNumber)) throw new IllegalArgumentException("Same account");
        Account from = getByAccountNumber(fromAccountNumber); ensureActive(from);
        Account to = getByAccountNumber(toAccountNumber);ensureActive(to);
        Object firstLock = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Object secondLock = firstLock == from ? to : from;
        synchronized (firstLock) {
            synchronized (secondLock) {
                if (from.getBalance() < amount) { throw new InsufficientBalanceException("Insufficient from " + fromAccountNumber); }
                        from.setBalance(from.getBalance() - amount); to.setBalance(to.getBalance() + amount);
                    accountRepository.save(from); accountRepository.save(to);
                }
            }
        Transaction tx = new Transaction(from.getId(), "TRANSFER", amount, Instant.now(), "transfer to " + toAccountNumber, fromAccountNumber, toAccountNumber);
                    Transaction saved = transactionRepository.save(tx); from.addTransaction(saved); accountRepository.save(from);
            log.info("Transferred {} from {} to {}", amount, fromAccountNumber, toAccountNumber);
            return saved;
        }

        @Override
        public List<Transaction> getTransactions(String accountNumber) {
            Account acc = getByAccountNumber(accountNumber);
            return transactionRepository.findByAccountId(acc.getId());
        }
    @Override
    public Account closeAccount(String accountNumber) {
        // I am reusing existing method so that AccountNotFoundException logic stays in one place.
        Account acc = getByAccountNumber(accountNumber);

        // I am choosing to allow closing only if balance is 0 so that money is not lost magically.
        if (acc.getBalance() != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + accountNumber);
        }

        // I am marking the status as INACTIVE instead of deleting it so that history is preserved.
        acc.setStatus("INACTIVE");
        Account saved = accountRepository.save(acc);
        log.info("Closed account {} by setting status INACTIVE", accountNumber);
        return saved;
    }

}
