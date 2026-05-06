package com.example.transaction_service.entity;

import com.example.transaction_service.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "Transactions")
public class Transaction {

    @Id
    @SequenceGenerator(name = "Tran_Seq",allocationSize = 1,initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "Tran_Seq")
    private Long transactionId;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false,precision = 19,scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Status status;

    @Column(nullable = false,unique = true,length = 100)
    private String idempotencyKey;
}
