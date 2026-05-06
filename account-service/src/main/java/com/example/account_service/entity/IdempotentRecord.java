package com.example.account_service.entity;

import com.example.account_service.enums.Action;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Table(name = "idempotency_records")
public class IdempotentRecord {
    @Id
    @Column(name = "idempotency_key",length = 100,nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Action action;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
