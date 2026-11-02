package bank.accounts.dto;

import java.math.BigDecimal;

public record BalanceOperationRequestDto(BigDecimal amount) {
}
