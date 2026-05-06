package com.example.account_service.dto;

import com.example.account_service.enums.AccountType;
import com.example.account_service.enums.Currency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAccountRequest {


    @NotNull(message = "account type is required")
    private AccountType accountType;
    @NotNull(message = "currency is required")
    private Currency currency;
    @NotNull(message = "initialDeposit cannot be null")
    @Min(value = 0,message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;
}
