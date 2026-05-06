package com.example.account_service.repository;

import com.example.account_service.entity.IdempotentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotentRecord,String> {
}
