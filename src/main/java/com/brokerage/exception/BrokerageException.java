package com.brokerage.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BrokerageException extends RuntimeException {
    
    private final HttpStatus status;
    
    public BrokerageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    public BrokerageException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}