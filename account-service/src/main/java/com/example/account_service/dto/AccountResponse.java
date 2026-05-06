package com.example.account_service.dto;

import com.example.account_service.enums.AccountType;
import com.example.account_service.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountResponse {
    private Long accountId;
    private String userId;
    private AccountType accountType;
    private BigDecimal balance;
    private Currency currency;
}
