package bank.cash.client;

import bank.cash.dto.AccountBalanceDto;
import bank.cash.exception.InsufficientFundsException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.client.RestClient;

public class AccountsClient {

    private final RestClient restClient;

    public AccountsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "accounts")
    public AccountBalanceDto debit(String login, BigDecimal amount) {
        return restClient.post()
                .uri("/internal/accounts/{login}/debit", login)
                .body(Map.of("amount", amount))
                .retrieve()
                .onStatus(s -> s.value() == 402, (req, res) -> {
                    throw new InsufficientFundsException("Недостаточно средств на счёте");
                })
                .body(AccountBalanceDto.class);
    }

    @CircuitBreaker(name = "accounts")
    public AccountBalanceDto credit(String login, BigDecimal amount) {
        return restClient.post()
                .uri("/internal/accounts/{login}/credit", login)
                .body(Map.of("amount", amount))
                .retrieve()
                .body(AccountBalanceDto.class);
    }
}
