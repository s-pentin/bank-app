package bank.accounts.dto;

import java.time.LocalDate;

public record AccountUpdateRequestDto(String firstName, String lastName, LocalDate birthDate) {
}
