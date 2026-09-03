package bank.transfer.dto;

import java.math.BigDecimal;

public record TransferRequestDto(String toLogin, BigDecimal amount) {
}
