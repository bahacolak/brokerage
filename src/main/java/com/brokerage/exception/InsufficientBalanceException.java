package com.brokerage.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BrokerageException {
    
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}