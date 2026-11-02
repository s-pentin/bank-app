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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TransferService {

    private final AccountsClient accountsClient;
    private final TransferOperationRepository transferOperationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionTemplate transactionTemplate;

    public TransferService(AccountsClient accountsClient,
                           TransferOperationRepository transferOperationRepository,
                           OutboxEventRepository outboxEventRepository,
                           TransactionTemplate transactionTemplate) {
        this.accountsClient = accountsClient;
        this.transferOperationRepository = transferOperationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public TransferResponseDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        validate(fromLogin, toLogin, amount);

        AccountBalanceDto debitResult = accountsClient.debit(fromLogin, amount);
        AccountBalanceDto creditResult;
        try {
            creditResult = accountsClient.credit(toLogin, amount);
        } catch (Exception e) {
            // компенсация при частичном отказе — вернуть деньги отправителю
            accountsClient.credit(fromLogin, amount);
            throw new TransferFailedException("Перевод не выполнен, средства возвращены", e);
        }

        saveOperation(fromLogin, toLogin, amount);
        return new TransferResponseDto(true, debitResult.balance(), "Перевод выполнен");
    }

    private void saveOperation(String fromLogin, String toLogin, BigDecimal amount) {
        transactionTemplate.executeWithoutResult(status -> {
            transferOperationRepository.save(new TransferOperation(fromLogin, toLogin, amount));
            outboxEventRepository.save(new OutboxEvent(fromLogin, "Переведено " + amount + " -> " + toLogin));
        });
    }

    private void validate(String fromLogin, String toLogin, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (fromLogin.equals(toLogin)) {
            throw new SameAccountTransferException("Нельзя перевести на свой же счёт");
        }
    }
}
