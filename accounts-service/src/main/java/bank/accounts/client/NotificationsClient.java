package bank.accounts.client;

import java.util.Map;
import org.springframework.web.client.RestClient;

public class NotificationsClient {

    private final RestClient restClient;

    public NotificationsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void notify(String recipientLogin, String message) {
        restClient.post()
                .uri("/internal/notifications")
                .body(Map.of("recipientLogin", recipientLogin, "message", message))
                .retrieve()
                .toBodilessEntity();
    }
}
