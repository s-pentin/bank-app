package bank.cash.service;

import bank.cash.client.AccountsClient;
import bank.cash.dto.AccountBalanceDto;
import bank.cash.dto.CashOperationResponseDto;
import bank.cash.model.CashOperation;
import bank.cash.model.CashOperationType;
import bank.cash.model.OutboxEvent;
import bank.cash.repository.CashOperationRepository;
import bank.cash.repository.OutboxEventRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final CashOperationRepository cashOperationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionTemplate transactionTemplate;

    public CashService(AccountsClient accountsClient,
                       CashOperationRepository cashOperationRepository,
                       OutboxEventRepository outboxEventRepository,
                       TransactionTemplate transactionTemplate) {
        this.accountsClient = accountsClient;
        this.cashOperationRepository = cashOperationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public CashOperationResponseDto deposit(String login, BigDecimal amount) {
        validateAmount(amount);
        AccountBalanceDto result = accountsClient.credit(login, amount);
        saveOperation(login, CashOperationType.DEPOSIT, amount);
        return new CashOperationResponseDto(true, result.balance(), "Пополнение выполнено");
    }

    public CashOperationResponseDto withdraw(String login, BigDecimal amount) {
        validateAmount(amount);
        AccountBalanceDto result = accountsClient.debit(login, amount);
        saveOperation(login, CashOperationType.WITHDRAW, amount);
        return new CashOperationResponseDto(true, result.balance(), "Снятие выполнено");
    }

    private void saveOperation(String login, CashOperationType type, BigDecimal amount) {
        transactionTemplate.executeWithoutResult(status -> {
            cashOperationRepository.save(new CashOperation(login, type, amount));
            outboxEventRepository.save(new OutboxEvent(login, type + " " + amount));
        });
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
    }
}
