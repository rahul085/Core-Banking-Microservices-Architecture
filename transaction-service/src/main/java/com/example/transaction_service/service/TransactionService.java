package com.example.transaction_service.service;

import com.example.transaction_service.config.TemporalConfig;
import com.example.transaction_service.context.UserContext;
import com.example.transaction_service.dto.AccountResponse;
import com.example.transaction_service.dto.TransactionEvent;
import com.example.transaction_service.dto.TransactionResponse;
import com.example.transaction_service.dto.TransferRequest;
import com.example.transaction_service.entity.OutboxEvent;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.enums.Status;
import com.example.transaction_service.exception.InvalidTransferException;
import com.example.transaction_service.exception.ResourceNotFoundException;
import com.example.transaction_service.repository.OutboxEventRepository;
import com.example.transaction_service.repository.TransactionRepository;
import com.example.transaction_service.workflow.FundTransferWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService{
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionRepository transactionRepository;
    private final WorkflowClient workflowClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate=new RestTemplate();

    public TransactionResponse initiateTransaction(TransferRequest dto, String idempotency){
        if(dto.getFromAccountId().equals(dto.getToAccountId())){
            throw new InvalidTransferException("cannot transfer money to the same account");
        }

        //  1. Grab the ID of the person making the HTTP request
        String loggedInUserId = UserContext.getUserId();

        if (loggedInUserId == null) {
            throw new RuntimeException("Unauthorized: User ID is missing from the request");
        }

        verifyAccountOwnership(dto.getFromAccountId(),loggedInUserId);

//        //  2. THE SECURITY PATCH (BOLA/IDOR Prevention)
//        try {
//            // Ask the Account Service who owns this 'fromAccount'
//            // (Make sure the URL matches your Account Service port/path!)
//            AccountResponse senderAccount = restTemplate.getForObject(
//                    "http://localhost:7002/api/v1/accounts/" + dto.getFromAccountId(),
//                    AccountResponse.class
//            );
//
//            // Compare the owner of the account to the person holding the JWT token
//            if (senderAccount == null || !senderAccount.getUserId().equals(loggedInUserId)) {
//                log.warn("🚨 SECURITY ALERT: User {} attempted to transfer funds from Account {} which they do not own!", loggedInUserId, dto.getFromAccountId());
//                throw new SecurityException("Forbidden: You do not have permission to initiate transfers from this account.");
//            }
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new RuntimeException("The sender account does not exist.");
//        }

        // Idempotency check
        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotency);
        if(existingTransaction.isPresent()){
            log.info("Duplicate request detected for Idempotency key: {}. Returning existing status.",idempotency);
            return mapToResponse(existingTransaction.get());
        }

        Transaction transaction=Transaction.builder()
                .fromAccountId(dto.getFromAccountId())
                .toAccountId(dto.getToAccountId())
                .amount(dto.getAmount())
                .status(Status.PENDING)
                .idempotencyKey(idempotency)
                .build();

        try{
            transaction=transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException ex){
            log.warn("Multiple transfer requests at the same time.... ");
            Transaction concurrentTransaction = transactionRepository.findByIdempotencyKey(idempotency).orElseThrow();
            return mapToResponse(concurrentTransaction);

        }
        // grab the user id sent by the api gateway before starting temporal.
        String userId = UserContext.getUserId();
        // Kick of the temporal workflow asynchronously.
        // We only reach this line if the transaction was genuinely new and successfully saved.
        log.info("Transaction saved as PENDING. Kicking of temporal workflow for Transaction ID: {}",transaction.getTransactionId());

        WorkflowOptions workflowOptions=WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TRANSACTION_TASK_QUEUE)
                .setWorkflowId("TransferWorkflow-"+transaction.getTransactionId())
                .build();

        FundTransferWorkflow fundTransferWorkflow = workflowClient.newWorkflowStub(FundTransferWorkflow.class, workflowOptions);
        // WorkflowClient.start() pushes the job to the queue and immediately moves to the next line of code
        WorkflowClient.start(
                fundTransferWorkflow::transfer,
                transaction.getTransactionId(),
                dto.getFromAccountId(),
                dto.getToAccountId(),
                dto.getAmount(),
                idempotency,
                userId

        );
        return mapToResponse(transaction);
    }

    @Transactional
    public void completeTransactionAndCreateOutbox(Long transactionId, Status finalStatus, String senderUserId, String receiverUserId, BigDecimal amount){
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() ->
                new ResourceNotFoundException("Transaction with id " + transactionId + " not found"));
        transaction.setStatus(finalStatus);
        transactionRepository.save(transaction);


        TransactionEvent transactionEvent=new TransactionEvent();
        transactionEvent.setTransactionId(transactionId.toString());
        transactionEvent.setAmount(amount);
        transactionEvent.setFromUserId(senderUserId);
        transactionEvent.setToUserId(receiverUserId);
        transactionEvent.setStatus(finalStatus.toString());

        try{
            OutboxEvent outboxEvent=new OutboxEvent();
            outboxEvent.setAggregateType("TRANSACTION");
            outboxEvent.setAggregateId(transactionId);
            outboxEvent.setPayload(objectMapper.writeValueAsString(transactionEvent));
            outboxEvent.setStatus("PENDING");
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);
            log.info("Outbox event created successfully for Transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to serialize and save outbox event", e);
            throw new RuntimeException("Failed to serialize and save outbox event", e);
        }
    }







private TransactionResponse mapToResponse(Transaction transaction){
    return new TransactionResponse(
            transaction.getTransactionId(),
            transaction.getFromAccountId(),
            transaction.getToAccountId(),
            transaction.getAmount(),
            transaction.getStatus()
    );
}
    @CircuitBreaker(name = "accountServiceCb", fallbackMethod = "fallbackVerifyAccountOwnership")
 private void verifyAccountOwnership(Long accountId, String loggedInUserId){
     log.info("Making network call to Account Service to verify ownership...");
     AccountResponse senderAccount = restTemplate.getForObject(
                   "http://localhost:7002/api/v1/accounts/" + accountId,
                   AccountResponse.class
           );

     if (senderAccount == null || !senderAccount.getUserId().equals(loggedInUserId)) {
         log.warn(" SECURITY ALERT: User {} attempted to transfer funds from Account {}!", loggedInUserId, accountId);
         throw new SecurityException("Forbidden: You do not have permission.");
     }

 }


 // Fallback method
 private void fallbackVerifyAccountOwnership(Long accountId, String loggedInUserId, Throwable throwable) {
     log.error("Account Service is unreachable or Circuit is OPEN. Blocking transaction for safety.");

     // Because this is a security check, our fallback cannot be "just let them through".
     // Our fallback MUST be to safely reject the request to protect the system.
     throw new RuntimeException("Account Verification Service is currently down. Please try your transfer later.");
 }

}


