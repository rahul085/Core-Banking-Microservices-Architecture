package com.example.transaction_service.workflow;

import com.example.transaction_service.activity.AccountActivities;
import com.example.transaction_service.enums.Status;
import com.example.transaction_service.exception.DownStreamValidationException;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;

import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

public class FundTransferWorkflowImpl implements FundTransferWorkflow{
    // 1. Use Temporal's replay-aware logger
    private static final Logger log = Workflow.getLogger(FundTransferWorkflowImpl.class);
    private final ActivityOptions accountOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(15))
            .setTaskQueue("TRANSACTION_TASK_QUEUE")
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setMaximumAttempts(3)
                    .setDoNotRetry(DownStreamValidationException.class.getName())
                    .build())
            .build();
private final AccountActivities accountActivities=Workflow.newActivityStub(AccountActivities.class,accountOptions);


    @Override
    public void transfer(Long transactionId, Long fromAccountId, Long toAccountId, BigDecimal amount, String idempotencyKey,String userId) {
        Saga saga=new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());

        try{
            log.info("Starting Transfer {}: Debiting account {}", transactionId, fromAccountId);
            accountActivities.debit(fromAccountId,amount,idempotencyKey);
            saga.addCompensation(accountActivities::compensateDebit,fromAccountId,amount,idempotencyKey);

            log.info("Transfer {}: Crediting account {}", transactionId, toAccountId);
            accountActivities.credit(toAccountId,amount,idempotencyKey);
            saga.addCompensation(accountActivities::compensateCredit,toAccountId,amount,idempotencyKey);

            accountActivities.completeTransaction(transactionId,Status.SUCCESS,userId,toAccountId,amount);


        } catch(ActivityFailure e){
            log.info("Saga Failed! Triggering Compensating transactions for Transaction: {}",transactionId);
            saga.compensate();
            accountActivities.completeTransaction(transactionId, Status.FAILED,userId,toAccountId,amount);
            throw Workflow.wrap(e);

        }

    }


}
