package com.example.account_service.entity;

import com.example.account_service.enums.AccountType;
import com.example.account_service.enums.Currency;
import com.example.account_service.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@Table(name = "accounts")
public class Account {
    @Id
    @SequenceGenerator(name = "account_seq",allocationSize = 1,initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "account_seq")
    private Long accountId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private AccountType accountType;

    @Column(nullable = false,precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false,precision = 19, scale = 4)
    private BigDecimal minBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Status status;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
