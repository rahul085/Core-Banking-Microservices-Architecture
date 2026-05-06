package com.example.transaction_service.activity;

import com.example.transaction_service.enums.Status;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.math.BigDecimal;

@ActivityInterface
public interface AccountActivities {
    @ActivityMethod
    void debit(Long accountId, BigDecimal amount,String idempotencyKey);

    @ActivityMethod
    void credit(Long accountId,BigDecimal amount,String idempotencyKey);

    @ActivityMethod
    void compensateCredit(Long accountId, BigDecimal amount, String idempotencyKey);

    @ActivityMethod
    void compensateDebit(Long accountId,BigDecimal amount,String idempotencyKey);

    @ActivityMethod
    void completeTransaction(Long transactionId, Status status, String senderUserId, Long toAccountId, BigDecimal amount);
}
