package com.ryzzlab.bankapp;

import jakarta.validation.Valid;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private AccountService accountService;
    @PostMapping("create")
    public ResponseEntity<?> createAccount(Principal principal){
        UUID userId =  UUID.fromString(principal.getName());
        accountService.createAccount(userId);
        return ResponseEntity.ok().body("Created successfully");
    }

    @GetMapping("balance")
    public ResponseEntity<?> get_balance(Principal principal){
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(
                accountService.getBalance(userId)
        );

    }

    @PostMapping("deposit")
    @Transactional
    public ResponseEntity<?> deposit(@RequestBody @jakarta.validation.Valid DepositRequest request,Principal principal){
        return ResponseEntity.ok(
                accountService.deposit(
                        UUID.fromString(principal.getName()),
                        request.getAmount()
                )
        );
    }

    @PostMapping("transfer")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                accountService.transfer(
                        UUID.fromString(principal.getName()),
                        request.getAmount(),
                        request.getToIban()
                )
        );
    }

    @GetMapping("transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam int page,
            @RequestParam int size,
            Principal principal
    ){
        return ResponseEntity.ok(
                accountService.getTransactions(UUID.fromString(principal.getName()),page,size)
        );
    }
    @GetMapping("details")
    public ResponseEntity<?> getDetails(
        Principal principal
    ){
        return ResponseEntity.ok(
                accountService.getDetails(UUID.fromString(principal.getName()))
        );
    }
}
