package com.example.account_service.service;

import com.example.account_service.context.UserContext;
import com.example.account_service.dto.AccountResponse;
import com.example.account_service.dto.CreateAccountRequest;
import com.example.account_service.entity.Account;
import com.example.account_service.entity.IdempotentRecord;
import com.example.account_service.enums.AccountType;
import com.example.account_service.enums.Action;
import com.example.account_service.enums.Status;
import com.example.account_service.exception.InsufficientFundsException;
import com.example.account_service.exception.ResourceNotFoundException;
import com.example.account_service.repository.AccountRepository;
import com.example.account_service.repository.IdempotencyRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final IdempotencyRepository idempotencyRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request){

        String loggedInUserIdString = UserContext.getUserId();

        if (loggedInUserIdString == null) {
            throw new RuntimeException("Unauthorized: User ID is missing from the request context");
        }

        BigDecimal minBalance = calculateMinBalance(request.getAccountType());

        if(request.getInitialDeposit().compareTo(minBalance)<0){
            throw new IllegalArgumentException("Initial deposit must be at least "+minBalance);
        }


      Account newAccount=Account.builder().
              userId(loggedInUserIdString)
              .accountType(request.getAccountType())
              .balance(request.getInitialDeposit())
              .currency(request.getCurrency())
              .minBalance(minBalance)
              .status(Status.ACTIVE)
              .build();

        Account savedAccount = accountRepository.save(newAccount);


        return new AccountResponse(
                savedAccount.getAccountId(),
                savedAccount.getUserId(),
                savedAccount.getAccountType(),
                savedAccount.getBalance(),
                savedAccount.getCurrency()

        );
    }

    public AccountResponse getAccountById(Long accountId){
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("An account with id " + accountId + " does not exist"));
        AccountResponse response=new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setCurrency(account.getCurrency());
        response.setAccountType(account.getAccountType());
        response.setUserId(account.getUserId());
        response.setBalance(account.getBalance());
        return response;

    }


    @Transactional
    public void creditAmount(Long accountId,BigDecimal amount,String idempotencyKey){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount should not be zero or negative");
        }
            // 1. The Idempotency Check (Look before we leap)
        if(idempotencyRepository.existsById(idempotencyKey)){
            log.info("Duplicate request detected for the key: {}. Ignoring gracefully.",idempotencyKey);
            return; // We don't throw an error. We just return success because the money is already there!
        }


        //2. Fetch the account
        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("Account not found with id: " + accountId));

        //3. Update the balance (Hibernate will check the @Version field here during commit)
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        //4 Save the idempotency key so we never process it again
        IdempotentRecord record=IdempotentRecord.builder()
                .idempotencyKey(idempotencyKey)
                .accountId(accountId)
                .action(Action.CREDIT)
                .build();
        idempotencyRepository.save(record);


    }

    @Transactional
    public void debitAmount(Long accountId,BigDecimal amount,String idempotencyKey){



        if(idempotencyRepository.existsById(idempotencyKey)){
            log.info("Duplicate request detected for the key: {}. Ignoring gracefully",idempotencyKey);
            return;
        }

        Account account = accountRepository.findById(accountId).orElseThrow(() ->
                new ResourceNotFoundException("Account with id " + accountId + " not found"));
        if(account.getBalance().compareTo(amount)<0){
            throw new InsufficientFundsException("Balance is less than the amount you want to transfer");

        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        IdempotentRecord record=IdempotentRecord.builder()
                .idempotencyKey(idempotencyKey)
                .accountId(accountId)
                .action(Action.DEBIT)
                .build();
        idempotencyRepository.save(record);
    }


    private BigDecimal calculateMinBalance(AccountType accountType){
        return switch (accountType){
            case SALARY -> BigDecimal.ZERO;
            case SAVINGS -> new BigDecimal("1000.00");
            case FIXED_DEPOSIT -> new BigDecimal("5000.00");

        };
    }
}
