package com.example.transaction_service.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.math.BigDecimal;

@WorkflowInterface
public interface FundTransferWorkflow {
    @WorkflowMethod
    void transfer(Long transactionId, Long fromAccountId, Long toAccountId, BigDecimal amount,String idempotencyKey,String senderUserId);
}
