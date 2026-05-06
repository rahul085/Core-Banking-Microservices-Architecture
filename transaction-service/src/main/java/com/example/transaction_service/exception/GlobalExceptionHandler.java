package com.example.transaction_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidTransferException.class)
    public ProblemDetail handleInvalidTransfer(InvalidTransferException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid transfer request");
        return problemDetail;

    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Insufficient funds");
        return problemDetail;

    }

    @ExceptionHandler(DownStreamValidationException.class)
    public ProblemDetail handleDownStreamValidation(DownStreamValidationException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Downstream service error");
        return problemDetail;
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource not found");
        return problemDetail;
    }
}
