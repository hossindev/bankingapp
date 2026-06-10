package com.ryzzlab.bankapp;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
}
