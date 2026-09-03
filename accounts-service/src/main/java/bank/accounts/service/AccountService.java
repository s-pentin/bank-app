package bank.accounts.service;

import bank.accounts.dto.AccountLookupDto;
import bank.accounts.dto.AccountResponseDto;
import bank.accounts.dto.AccountUpdateRequestDto;
import bank.accounts.exception.AccountNotFoundException;
import bank.accounts.exception.InsufficientFundsException;
import bank.accounts.exception.ValidationException;
import bank.accounts.model.Account;
import bank.accounts.model.OutboxEvent;
import bank.accounts.repository.AccountRepository;
import bank.accounts.repository.OutboxEventRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final OutboxEventRepository outboxEventRepository;

    public AccountService(AccountRepository accountRepository, OutboxEventRepository outboxEventRepository) {
        this.accountRepository = accountRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public AccountResponseDto getMyAccount(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseGet(() -> accountRepository.save(new Account(login, BigDecimal.ZERO)));
        return toResponseDto(account);
    }

    @Transactional
    public AccountResponseDto updateMyAccount(String login, AccountUpdateRequestDto dto) {
        validate(dto);
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Аккаунт " + login + " не найден"));
        account.setFirstName(dto.firstName());
        account.setLastName(dto.lastName());
        account.setBirthDate(dto.birthDate());
        return toResponseDto(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountLookupDto> lookup(String currentLogin, String query) {
        return accountRepository.findAll().stream()
                .filter(a -> !a.getLogin().equals(currentLogin))
                .filter(a -> query == null || query.isBlank()
                        || a.getLogin().toLowerCase().contains(query.toLowerCase())
                        || (a.getFirstName() != null && a.getFirstName().toLowerCase().contains(query.toLowerCase()))
                        || (a.getLastName() != null && a.getLastName().toLowerCase().contains(query.toLowerCase())))
                .map(a -> new AccountLookupDto(a.getLogin(), a.getFirstName(), a.getLastName()))
                .toList();
    }

    @Transactional
    public AccountResponseDto debit(String login, BigDecimal amount) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Аккаунт " + login + " не найден"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на счёте");
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        outboxEventRepository.save(new OutboxEvent(login, "Списано " + amount));
        return toResponseDto(saved);
    }

    @Transactional
    public AccountResponseDto credit(String login, BigDecimal amount) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Аккаунт " + login + " не найден"));
        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        outboxEventRepository.save(new OutboxEvent(login, "Пополнено " + amount));
        return toResponseDto(saved);
    }

    private void validate(AccountUpdateRequestDto dto) {
        if (dto.firstName() == null || dto.firstName().isBlank()
                || dto.lastName() == null || dto.lastName().isBlank()
                || dto.birthDate() == null) {
            throw new ValidationException("Все поля должны быть заполнены");
        }
        if (Period.between(dto.birthDate(), LocalDate.now()).getYears() < 18) {
            throw new ValidationException("Возраст должен быть не менее 18 лет");
        }
    }

    private AccountResponseDto toResponseDto(Account account) {
        return new AccountResponseDto(
                account.getLogin(),
                account.getFirstName(),
                account.getLastName(),
                account.getBirthDate(),
                account.getBalance());
    }
}
