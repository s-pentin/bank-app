package bank.transfer.dto;

import java.math.BigDecimal;

public record TransferResponseDto(boolean success, BigDecimal newBalance, String message) {
}
