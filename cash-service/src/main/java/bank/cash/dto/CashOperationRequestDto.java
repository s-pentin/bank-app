package bank.cash.dto;

import java.math.BigDecimal;

public record CashOperationRequestDto(BigDecimal amount) {
}
