package com.ryzzlab.bankapp;

import jakarta.transaction.Transactional;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/account/")
public class AccountController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @PostMapping("create")
    public ResponseEntity<?> create_account(Principal principal){
        String userId =  principal.getName();
        if(accountRepository.findByUser_Id(UUID.fromString(userId)).isPresent()){
            return ResponseEntity.status(400).body("Account already exists");
        }
        Account newAccount = new Account();
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow();
        newAccount.setUser(user);
        newAccount.setBalance(0);
        newAccount.setIban(Iban.random(CountryCode.DE).toString());
        accountRepository.save(newAccount);
        return ResponseEntity.ok().body("Created successfully");
    }

    @GetMapping("balance")
    public ResponseEntity<?> get_balance(Principal principal){
        String userId = principal.getName();
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(UUID.fromString(userId));
        if(optionalAccount.isEmpty()){
            return ResponseEntity.status(404).body("Account not found");
        }
        return ResponseEntity.ok().body(optionalAccount.get().getBalance());
    }

    @PostMapping("deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, String> body,Principal principal){
        String userId = principal.getName();
        Integer amount =  Integer.parseInt(body.get("amount"));
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(UUID.fromString(userId));
        if (optionalAccount.isEmpty() ||  amount <= 0 ){
            return ResponseEntity.badRequest().body("Invalid deposit");
        }
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Account not found"));
        Account account = optionalAccount.get();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setFromIban(null);
        transaction.setToIban(account.getIban());
        transaction.setCreatedAt(LocalDateTime.now());
        Map<String, Object> response = new HashMap<>();

        response.put("transaction", transaction);
        response.put("account", account);
        transactionRepository.save(transaction);
        return ResponseEntity.ok(response);
    }

    @PostMapping("transfer")
    @Transactional
    public ResponseEntity<?> transfer(@RequestBody Map<String, String > body, Principal principal){
        UUID userId = UUID.fromString(principal.getName());
        Integer amount = Integer.parseInt(body.get("amount"));
        String toIban = body.get("toIban");


        Optional<Account> optionalAccount = accountRepository.findByUser_Id(userId);
        if (optionalAccount.isEmpty() ||  amount <= 0 || optionalAccount.get().getBalance() < amount ){
            return ResponseEntity.badRequest().body("Invalid deposit");
        }
        String fromIban = optionalAccount.get().getIban();
        Account fromAccount = optionalAccount.get();
        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        Transaction transaction = new Transaction();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Account not found"));
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setToIban(toIban);
        transaction.setFromIban(fromIban);
        transaction.setCreatedAt(LocalDateTime.now());


        Map<String, Object> response = new HashMap<>();

        response.put("transaction", transaction);
        response.put("fromAccount", fromAccount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionRepository.save(transaction);
        return ResponseEntity.ok(response);
    }

    @GetMapping("transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam int page,
            @RequestParam int size,
            Principal principal
    ){
        UUID userId = UUID.fromString(principal.getName());
        Account account = accountRepository.findByUser_Id(userId)
                .orElseThrow();

        String iban = account.getIban();

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions =
                transactionRepository.findByFromIbanOrToIban(iban, iban, pageable);

        return ResponseEntity.ok(transactions);
    }
    @GetMapping("details")
    public ResponseEntity<?> getDetails(
        Principal principal
    ){
        UUID userId = UUID.fromString(principal.getName());
        Optional<Account> account = accountRepository.findByUser_Id(userId);
        if(account.isEmpty()){
            return ResponseEntity.badRequest().body("Invalid account");
        }
        return ResponseEntity.ok().body(account);
    }
}
