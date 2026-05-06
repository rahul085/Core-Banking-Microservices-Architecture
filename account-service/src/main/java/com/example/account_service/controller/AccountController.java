package com.example.account_service.controller;

import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.AmountRequest;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
     private  final AccountService accountService;

     @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request){
         return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
     }

     @PostMapping("/{accountId}/credit")
    public ResponseEntity<String> creditAmount(@PathVariable Long accountId,
                                               @RequestHeader("idempotency-key") String idempotencyKey,
                                               @Valid @RequestBody AmountRequest amountRequest){
         accountService.creditAmount(accountId,amountRequest.getAmount(),idempotencyKey);
         return new ResponseEntity<>("Credit of amount: "+amountRequest.getAmount()+" is successful to the account: "+accountId
                 ,HttpStatus.OK);
     }

     @PostMapping("/{accountId}/debit")
    public ResponseEntity<String> debitAmount(@PathVariable Long accountId,
                                              @RequestHeader("idempotency-key")String idempotencyKey,
                                              @Valid @RequestBody AmountRequest amountRequest){
         accountService.debitAmount(accountId,amountRequest.getAmount(),idempotencyKey);
         return new ResponseEntity<>("Debit of amount: "+amountRequest.getAmount()+" is successful from the account "+accountId,
                 HttpStatus.OK);
     }

     @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId){
         return new ResponseEntity<>(accountService.getAccountById(accountId),HttpStatus.OK);
     }
}
