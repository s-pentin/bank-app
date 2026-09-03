package bank.cash.exception;

public class AccountsServiceUnavailableException extends RuntimeException {
    public AccountsServiceUnavailableException(String message) {
        super(message);
    }
}
