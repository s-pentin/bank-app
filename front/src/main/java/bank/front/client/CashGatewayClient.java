package bank.front.client;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CashGatewayClient {

    private final RestClient restClient;

    public CashGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void deposit(BigDecimal amount) {
        restClient.post()
                .uri("/api/v1/cash/deposit")
                .body(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity();
    }

    public void withdraw(BigDecimal amount) {
        restClient.post()
                .uri("/api/v1/cash/withdraw")
                .body(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity();
    }
}
