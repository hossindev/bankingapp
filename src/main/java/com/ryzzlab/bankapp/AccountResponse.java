package com.ryzzlab.bankapp;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AccountResponse {
    private UUID id;
    private String iban;
    private Long balance;

    public AccountResponse(Account account){
        this.id = account.getId();
        this.iban = account.getIban();
        this.balance = account.getBalance();
    }
}
