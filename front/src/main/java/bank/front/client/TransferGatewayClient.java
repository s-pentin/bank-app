package bank.front.client;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TransferGatewayClient {

    private final RestClient restClient;

    public TransferGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void transfer(String toLogin, BigDecimal amount) {
        restClient.post()
                .uri("/api/v1/transfers")
                .body(Map.of("toLogin", toLogin, "amount", amount))
                .retrieve()
                .toBodilessEntity();
    }
}
