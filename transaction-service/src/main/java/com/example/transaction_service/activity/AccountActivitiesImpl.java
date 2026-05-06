package com.example.transaction_service.activity;



import com.example.transaction_service.dto.AccountResponse;
import com.example.transaction_service.dto.AmountRequest;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.enums.Status;
import com.example.transaction_service.exception.ResourceNotFoundException;
import com.example.transaction_service.repository.TransactionRepository;
import com.example.transaction_service.service.TransactionService;
import com.example.transaction_service.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class AccountActivitiesImpl implements AccountActivities{
    private final WebClientUtil webClientUtil;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Override
    public void debit(Long accountId, BigDecimal amount, String idempotencyKey) {

        String actionKey=idempotencyKey+"_DEBIT";
       webClientUtil.exchange(
               HttpMethod.POST,
               "http://ACCOUNT-SERVICE/api/v1/accounts/"+accountId+"/debit",
               new AmountRequest(amount),
               Map.of("idempotency-key",actionKey),
               Void.class
       );

    }

    @Override
    public void credit(Long accountId, BigDecimal amount, String idempotencyKey) {
        String actionKey=idempotencyKey+"_CREDIT";
        webClientUtil.exchange(
                HttpMethod.POST,
                "http://ACCOUNT-SERVICE/api/v1/accounts/"+accountId+"/credit",
                new AmountRequest(amount),
                Map.of("idempotency-key",actionKey),
                Void.class
        );

    }

    @Override
    public void compensateCredit(Long accountId, BigDecimal amount, String idempotencyKey) {
        log.warn("SAGA ROLLBACK: Executing compensation for CREDIT. Refunding account: {}", accountId);
        String compensationKey=idempotencyKey+"_COMP_CREDIT";
        this.debit(accountId,amount,compensationKey);
    }

    @Override
    public void compensateDebit(Long accountId, BigDecimal amount, String idempotencyKey) {
        log.warn("SAGA ROLLBACK: Executing compensation for DEBIT. Refunding account: {}", accountId);
        String compensationKey=idempotencyKey+"_COMP_DEBIT";
        this.credit(accountId,amount,compensationKey);

    }

    @Override
    public void completeTransaction(Long transactionId, Status status,String senderUserId,Long toAccountId,BigDecimal amount) {
//

        String receiverUserId=null;
        try{
            log.info("Fetching account details from ACCOUNT-SERVICE for account ID: {}", toAccountId);
            // 1. Make the external call to get the Account details
            AccountResponse accountResponse = webClientUtil.exchange(
                    HttpMethod.GET,
                    "http://ACCOUNT-SERVICE/api/v1/accounts/" + toAccountId,
                    null, // No body needed for a GET request
                    null, // No special headers needed
                    AccountResponse.class
            );
            // 2. Extract the User ID
            receiverUserId = accountResponse.getUserId();
            log.info("Successfully found receiver User ID: {}", receiverUserId);
        }catch (Exception e) {
            log.error("Failed to fetch account details for accountId: {}", toAccountId, e);
            // Throwing an exception here is GOOD! It tells Temporal to retry this activity later.
            throw io.temporal.failure.ApplicationFailure.newFailure(
                    "Failed to fetch receiver User ID from Account Service",
                    "ACCOUNT_FETCH_ERROR"
            );
        }

        transactionService.completeTransactionAndCreateOutbox(
                transactionId,
                status,
                senderUserId,
                receiverUserId,
                amount
        );
    }
}
