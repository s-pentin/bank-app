package bank.front.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountViewDto(String login, String firstName, String lastName, LocalDate birthDate, BigDecimal balance) {
}
