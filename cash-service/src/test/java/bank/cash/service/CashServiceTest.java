package bank.cash.service;

import bank.cash.client.AccountsClient;
import bank.cash.dto.AccountBalanceDto;
import bank.cash.dto.CashOperationResponseDto;
import bank.cash.exception.InsufficientFundsException;
import bank.cash.model.CashOperation;
import bank.cash.model.OutboxEvent;
import bank.cash.repository.CashOperationRepository;
import bank.cash.repository.OutboxEventRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;
    @Mock
    private CashOperationRepository cashOperationRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private CashService cashService;

    @BeforeEach
    void setUp() {
        cashService = new CashService(accountsClient, cashOperationRepository, outboxEventRepository,
                new TransactionTemplate(transactionManager));
    }

    @Test
    void deposit_callsAccountsAndSavesOperationAndOutbox() {
        when(accountsClient.credit("ivan", new BigDecimal("100.00")))
                .thenReturn(new AccountBalanceDto(new BigDecimal("150.00")));

        CashOperationResponseDto result = cashService.deposit("ivan", new BigDecimal("100.00"));

        assertTrue(result.success());
        assertEquals(0, result.newBalance().compareTo(new BigDecimal("150.00")));
        verify(cashOperationRepository).save(any(CashOperation.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void withdraw_callsAccountsAndSavesOperationAndOutbox() {
        when(accountsClient.debit("ivan", new BigDecimal("30.00")))
                .thenReturn(new AccountBalanceDto(new BigDecimal("70.00")));

        CashOperationResponseDto result = cashService.withdraw("ivan", new BigDecimal("30.00"));

        assertTrue(result.success());
        assertEquals(0, result.newBalance().compareTo(new BigDecimal("70.00")));
        verify(cashOperationRepository).save(any(CashOperation.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void withdraw_insufficientFunds_doesNotSave() {
        when(accountsClient.debit("ivan", new BigDecimal("100.00")))
                .thenThrow(new InsufficientFundsException("Недостаточно средств"));

        assertThrows(InsufficientFundsException.class,
                () -> cashService.withdraw("ivan", new BigDecimal("100.00")));
        verify(cashOperationRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void deposit_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> cashService.deposit("ivan", BigDecimal.ZERO));
    }
}
