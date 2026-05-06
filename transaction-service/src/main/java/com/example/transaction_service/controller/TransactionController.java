package com.example.transaction_service.controller;

import com.example.transaction_service.dto.FundTransferDto;
import com.example.transaction_service.dto.TransactionResponse;
import com.example.transaction_service.dto.TransferRequest;
import com.example.transaction_service.service.TransactionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @RateLimiter(name="transactionRateLimit",fallbackMethod = "rateLimitFallback")
    public ResponseEntity<TransactionResponse> initiateTransaction(@Valid @RequestBody TransferRequest transferRequest, @RequestHeader("idempotency-key") String idempotencyKey){

        return new ResponseEntity<>(transactionService.initiateTransaction(transferRequest,idempotencyKey), HttpStatus.CREATED);
    }

    // RATE LIMIT FALLBACK

    // If the user exceeds 5 requests per second, Spring routes them here.
    public ResponseEntity<String> rateLimitFallback(String idempotencyKey, TransferRequest request, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("You are submitting transfers too quickly. Please wait a moment and try again.");
    }


}
