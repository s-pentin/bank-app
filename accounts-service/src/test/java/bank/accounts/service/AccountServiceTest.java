package bank.accounts.service;

import bank.accounts.dto.AccountResponseDto;
import bank.accounts.dto.AccountUpdateRequestDto;
import bank.accounts.exception.InsufficientFundsException;
import bank.accounts.exception.ValidationException;
import bank.accounts.model.Account;
import bank.accounts.repository.AccountRepository;
import bank.accounts.repository.OutboxEventRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, outboxEventRepository);
    }

    @Test
    void getMyAccount_createsAccountWhenNotExists() {
        when(accountRepository.findByLogin("ivan")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDto result = accountService.getMyAccount("ivan");

        assertEquals("ivan", result.login());
        assertEquals(0, result.balance().compareTo(BigDecimal.ZERO));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getMyAccount_returnsExistingAccount() {
        Account account = new Account("ivan", new BigDecimal("100.00"));
        when(accountRepository.findByLogin("ivan")).thenReturn(Optional.of(account));

        AccountResponseDto result = accountService.getMyAccount("ivan");

        assertEquals("ivan", result.login());
        assertEquals(0, result.balance().compareTo(new BigDecimal("100.00")));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateMyAccount_ageUnder18_throws() {
        AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                "Ivan", "Ivanov", LocalDate.now().minusYears(17));

        assertThrows(ValidationException.class,
                () -> accountService.updateMyAccount("ivan", dto));
    }

    @Test
    void updateMyAccount_blankFields_throws() {
        AccountUpdateRequestDto dto = new AccountUpdateRequestDto(
                "", "Ivanov", LocalDate.now().minusYears(20));

        assertThrows(ValidationException.class,
                () -> accountService.updateMyAccount("ivan", dto));
    }

    @Test
    void debit_success_updatesBalanceAndWritesOutbox() {
        Account account = new Account("ivan", new BigDecimal("100.00"));
        when(accountRepository.findByLogin("ivan")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDto result = accountService.debit("ivan", new BigDecimal("30.00"));

        assertEquals(0, result.balance().compareTo(new BigDecimal("70.00")));
        verify(outboxEventRepository).save(any());
    }

    @Test
    void debit_insufficientFunds_throws() {
        Account account = new Account("ivan", new BigDecimal("50.00"));
        when(accountRepository.findByLogin("ivan")).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.debit("ivan", new BigDecimal("100.00")));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void credit_success_updatesBalanceAndWritesOutbox() {
        Account account = new Account("ivan", new BigDecimal("100.00"));
        when(accountRepository.findByLogin("ivan")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDto result = accountService.credit("ivan", new BigDecimal("50.00"));

        assertEquals(0, result.balance().compareTo(new BigDecimal("150.00")));
        verify(outboxEventRepository).save(any());
    }
}
