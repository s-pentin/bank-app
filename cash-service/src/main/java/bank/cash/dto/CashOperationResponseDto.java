package bank.cash.dto;

import java.math.BigDecimal;

public record CashOperationResponseDto(boolean success, BigDecimal newBalance, String message) {
}
