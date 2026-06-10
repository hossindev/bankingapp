package com.ryzzlab.bankapp;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException( String message){
        super(message);
    }
}
