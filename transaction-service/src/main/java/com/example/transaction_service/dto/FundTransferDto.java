package com.example.transaction_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FundTransferDto {
    @NotNull(message = "fromAccountId is required..")
    private Long fromAccountId;
    @NotNull(message = "toAccountId is required....")
    private Long toAccountId;
    @NotNull(message = "amount is required....")
    private BigDecimal amount;
}
