package com.controller;
import com.dto.AmountRequest;
import com.dto.CreateAccountRequest;
import com.model.Account; import com.model.Transaction; import com.service.AccountService;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.net.URI; import java.util.List;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService svc;
    public AccountController(AccountService svc) { this.svc = svc; }

    // create using JSON body
    @PostMapping
    public ResponseEntity<Account> createJson(@Valid @RequestBody CreateAccountRequest req) {
        Account created = svc.createAccount(req.getName());
        return ResponseEntity.created(URI.create("/api/accounts/" + created.getAccountNumber())).body(created);
    }

    // create using path variable /api/accounts/{name}
    @PostMapping("/{name}")
    public ResponseEntity<Account> createByName(@PathVariable("name") @NotBlank String name) {
            Account created = svc.createAccount(name);
        return ResponseEntity.created(URI.create("/api/accounts/" + created.getAccountNumber())).body(created);
}

@GetMapping("/{accountNumber}")
public ResponseEntity<Account> get(@PathVariable String accountNumber) {
    Account acc = svc.getByAccountNumber(accountNumber);
    return ResponseEntity.ok(acc);
}

@DeleteMapping("/{accountNumber}")
public ResponseEntity<Void> delete(@PathVariable String accountNumber) {
    svc.deleteByAccountNumber(accountNumber);
    return ResponseEntity.noContent().build();
}

@PostMapping("/{accountNumber}/deposit")
public ResponseEntity<Transaction> deposit(@PathVariable String accountNumber,
                                           @RequestBody(required = false) AmountRequest body,
                                           @RequestParam(value = "amount", required = false) Long amountParam) {
                                                   long amount = amountParam != null ? amountParam : (body != null ? body.getAmount() : 0L);
Transaction tx = svc.deposit(accountNumber, amount);
        return ResponseEntity.ok(tx);
    }

@PostMapping("/{accountNumber}/withdraw")
public ResponseEntity<Transaction> withdraw(@PathVariable String accountNumber,
                                            @RequestBody(required = false) AmountRequest body,
                                            @RequestParam(value = "amount", required = false) Long amountParam) {
                                                    long amount = amountParam != null ? amountParam : (body != null ? body.getAmount() : 0L);
Transaction tx = svc.withdraw(accountNumber, amount);
        return ResponseEntity.ok(tx);
    }

@PostMapping("/{fromAccount}/transfer/{toAccount}")
public ResponseEntity<Transaction> transfer(@PathVariable("fromAccount") String fromAccount,
                                                    @PathVariable("toAccount") String toAccount,
                                                    @RequestBody(required = false) AmountRequest body,
                                            @RequestParam(value = "amount", required = false) Long amountParam) {
                                                    long amount = amountParam != null ? amountParam : (body != null ? body.getAmount() : 0L);
Transaction tx = svc.transfer(fromAccount, toAccount, amount);
        return ResponseEntity.ok(tx);
    }

@GetMapping("/{accountNumber}/transactions")
public ResponseEntity<List<Transaction>> transactions(@PathVariable String accountNumber) {
    List<Transaction> list = svc.getTransactions(accountNumber);
    return ResponseEntity.ok(list);
}
@PutMapping("/{accountNumber}/close")
public ResponseEntity<Account> close(@PathVariable String accountNumber) {
        Account acc = svc.closeAccount(accountNumber);
        return ResponseEntity.ok(acc);
    }

}
