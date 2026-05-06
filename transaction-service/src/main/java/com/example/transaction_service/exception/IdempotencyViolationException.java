package com.example.transaction_service.exception;

public class IdempotencyViolationException extends RuntimeException{
    public IdempotencyViolationException(String message){
        super(message);
    }
}
