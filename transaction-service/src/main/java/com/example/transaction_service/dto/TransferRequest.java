package com.example.transaction_service.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class TransferRequest {
    @NotNull(message = "Source account id cannot be null")
    private Long fromAccountId;
    @NotNull(message = "Destination account id cannot be null")
    private Long toAccountId;
    @NotNull(message = "amount cannot be null")
    @DecimalMin(value = "0.01",message = "amount should be greater than zero")
    private BigDecimal amount;
}
