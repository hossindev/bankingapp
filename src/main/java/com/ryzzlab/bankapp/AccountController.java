package com.ryzzlab.bankapp;

import jakarta.transaction.Transactional;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        if (optionalAccount.isEmpty() ||  amount < 0 ){
            return ResponseEntity.badRequest().body("Invalid deposit");
        }
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Account not found"));
        Account account = optionalAccount.get();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setUser(user);
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
        String iban = body.get("iban");
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(userId);
        if (optionalAccount.isEmpty() ||  amount < 0 || optionalAccount.get().getBalance() < amount ){
            return ResponseEntity.badRequest().body("Invalid deposit");
        }
        Account fromAccount = accountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Account toAccount = accountRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        Transaction transaction = new Transaction();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Account not found"));
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setToIban(iban);

        Map<String, Object> response = new HashMap<>();

        response.put("transaction", transaction);
        response.put("fromAccount", fromAccount);
        transactionRepository.save(transaction);
        return ResponseEntity.ok(response);
    }
}
