package bank.transfer.service;

import bank.transfer.client.AccountsClient;
import bank.transfer.dto.AccountBalanceDto;
import bank.transfer.dto.TransferResponseDto;
import bank.transfer.exception.SameAccountTransferException;
import bank.transfer.exception.TransferFailedException;
import bank.transfer.model.OutboxEvent;
import bank.transfer.model.TransferOperation;
import bank.transfer.repository.OutboxEventRepository;
import bank.transfer.repository.TransferOperationRepository;
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
class TransferServiceTest {

    @Mock
    private AccountsClient accountsClient;
    @Mock
    private TransferOperationRepository transferOperationRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountsClient, transferOperationRepository,
                outboxEventRepository, new TransactionTemplate(transactionManager));
    }

    @Test
    void transfer_success_debitsCreditsAndSaves() {
        when(accountsClient.debit("ivan", new BigDecimal("100.00")))
                .thenReturn(new AccountBalanceDto(new BigDecimal("50.00")));
        when(accountsClient.credit("petr", new BigDecimal("100.00")))
                .thenReturn(new AccountBalanceDto(new BigDecimal("200.00")));

        TransferResponseDto result = transferService.transfer("ivan", "petr", new BigDecimal("100.00"));

        assertTrue(result.success());
        assertEquals(0, result.newBalance().compareTo(new BigDecimal("50.00")));
        verify(transferOperationRepository).save(any(TransferOperation.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void transfer_sameAccount_throws() {
        assertThrows(SameAccountTransferException.class,
                () -> transferService.transfer("ivan", "ivan", new BigDecimal("100.00")));
    }

    @Test
    void transfer_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer("ivan", "petr", BigDecimal.ZERO));
    }

    @Test
    void transfer_creditFails_compensatesAndThrows() {
        when(accountsClient.debit("ivan", new BigDecimal("100.00")))
                .thenReturn(new AccountBalanceDto(new BigDecimal("50.00")));
        when(accountsClient.credit("petr", new BigDecimal("100.00")))
                .thenThrow(new RuntimeException("down"));

        assertThrows(TransferFailedException.class,
                () -> transferService.transfer("ivan", "petr", new BigDecimal("100.00")));
        verify(accountsClient).credit("ivan", new BigDecimal("100.00"));
        verify(transferOperationRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }
}
