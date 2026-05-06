package com.example.transaction_service.exception;

public class DownStreamValidationException extends RuntimeException{
    public DownStreamValidationException(String message){
        super(message);
    }
}
