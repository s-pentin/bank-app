package bank.transfer.exception;

public class TransferFailedException extends RuntimeException {

    public TransferFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
