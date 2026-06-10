package com.ryzzlab.bankapp;

public class AccountNotFoundException extends RuntimeException {
    public  AccountNotFoundException(String message){
        super(message);
    }
}
