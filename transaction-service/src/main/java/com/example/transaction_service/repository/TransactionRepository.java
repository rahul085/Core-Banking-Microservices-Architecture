package com.example.transaction_service.repository;

import com.example.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    Optional<Transaction> findByIdempotencyKey(String key);
}
