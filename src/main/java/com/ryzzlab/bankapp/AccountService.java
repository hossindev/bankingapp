package com.ryzzlab.bankapp;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TransactionRepository transactionRepository;

    public void createAccount(UUID userId){
        if(accountRepository.findByUser_Id(userId).isPresent()){
            throw new AccountAlreadyExistsException("Account already exists");
        }
        Account newAccount = new Account();
        User user = userRepository.findById(userId).orElseThrow();
        newAccount.setUser(user);
        newAccount.setBalance(0L);
        newAccount.setIban(Iban.random(CountryCode.DE).toString());
        accountRepository.save(newAccount);
    }
    public Long getBalance(UUID userId){
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(userId);
        if(optionalAccount.isEmpty()){
            throw new AccountNotFoundException("account not found");
        }
        return optionalAccount.get().getBalance();
    }
    @Transactional
    public AccountResponse deposit(UUID userId, Long amount){
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(userId);
        if (optionalAccount.isEmpty() ||  amount <= 0 ){
            throw new InsufficientFundsException("Invalid deposit");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new AccountNotFoundException("Account not found"));
        Account account = optionalAccount.get();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setFromIban(null);
        transaction.setToIban(account.getIban());
        transaction.setCreatedAt(LocalDateTime.now());



        transactionRepository.save(transaction);
        return new AccountResponse(account);
    }
    @Transactional
    public AccountResponse transfer(UUID userId, Long amount, String toIban){
        Optional<Account> optionalAccount = accountRepository.findByUser_Id(userId);
        if (optionalAccount.isEmpty() ||  amount <= 0 || optionalAccount.get().getBalance() < amount ){
            throw new InsufficientFundsException("Invalid transfer");
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


        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionRepository.save(transaction);
        return new AccountResponse(fromAccount);
    }
    public Page<Transaction> getTransactions(UUID userId, int page, int size){
        Account account = accountRepository.findByUser_Id(userId)
                .orElseThrow();

        String iban = account.getIban();

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions =
                transactionRepository.findByFromIbanOrToIban(iban, iban, pageable);

        return transactions;
    }
    public AccountResponse getDetails(UUID userId){
        Account account = accountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return new AccountResponse(account);
    }
}
