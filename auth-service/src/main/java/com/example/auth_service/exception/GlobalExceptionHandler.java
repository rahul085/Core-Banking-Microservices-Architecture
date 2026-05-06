package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("Resource not found");
        return new ResponseEntity<>(problemDetail,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidToken(InvalidTokenException ex){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        problemDetail.setTitle("Invalid token");
        return new ResponseEntity<>(problemDetail,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex){
        Map<String,Object> errorResponse=new HashMap<>();
        Map<String,String> errors=new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for(FieldError fieldError: fieldErrors){
            errors.put(fieldError.getField(),fieldError.getDefaultMessage());
        }
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status",HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message","Validation Failed!");
        errorResponse.put("errors",errors);

        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);

    }
}
