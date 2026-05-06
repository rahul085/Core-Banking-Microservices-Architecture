package com.example.account_service.service;

import com.example.account_service.entity.Account;
import com.example.account_service.exception.ResourceNotFoundException;
import com.example.account_service.repository.AccountRepository;
import com.example.account_service.repository.IdempotencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void successfulCredit_increasesBalanceAndSavesIdempotency() {
        String key = "key-123";
        Long accountId = 1L;
        BigDecimal initial = new BigDecimal("100.00");
        BigDecimal credit = new BigDecimal("50.00");

        when(idempotencyRepository.existsById(key)).thenReturn(false);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(initial);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.creditAmount(accountId, credit, key);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getBalance());

        verify(idempotencyRepository).save(any());
    }

    @Test
    void duplicateIdempotencyKey_noop_noRepositoryCalls() {
        String key = "dup-key";
        when(idempotencyRepository.existsById(key)).thenReturn(true);

        // Should return without exception and without touching account repository
        accountService.creditAmount(1L, new BigDecimal("10.00"), key);

        verify(accountRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any()); // already exists so we don't save again
    }

    @Test
    void negativeOrZeroAmount_throwsIllegalArgumentException() {
        String key = "k";
        assertThrows(IllegalArgumentException.class,
                () -> accountService.creditAmount(1L, BigDecimal.ZERO, key));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.creditAmount(1L, new BigDecimal("-5.00"), key));
    }

    @Test
    void accountNotFound_throwsResourceNotFoundException() {
        String key = "k2";
        when(idempotencyRepository.existsById(key)).thenReturn(false);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.creditAmount(99L, new BigDecimal("10.00"), key));
    }

    @Test
    void successfulDebit_decreasesBalanceAndSavesIdempotency() {
        String key = "debit-key";
        Long accountId = 2L;
        BigDecimal initial = new BigDecimal("200.00");
        BigDecimal debit = new BigDecimal("50.00");

        when(idempotencyRepository.existsById(key)).thenReturn(false);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(initial);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.debitAmount(accountId, debit, key);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getBalance());

        verify(idempotencyRepository).save(any());
    }

    @Test
    void debit_insufficientFunds_throwsInsufficientFundsException_andNoSaves() {
        String key = "debit-insufficient";
        Long accountId = 3L;
        BigDecimal initial = new BigDecimal("30.00");
        BigDecimal debit = new BigDecimal("50.00");

        when(idempotencyRepository.existsById(key)).thenReturn(false);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(initial);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(com.example.account_service.exception.InsufficientFundsException.class,
                () -> accountService.debitAmount(accountId, debit, key));

        verify(accountRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any());
    }

}
