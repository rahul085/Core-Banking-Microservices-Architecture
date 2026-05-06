package com.example.transaction_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OutboxEvent {
    @Id
    @SequenceGenerator(name = "OutboxSeq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "OutboxSeq")
    private Long id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}

