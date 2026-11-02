package bank.front.client;

import bank.front.dto.AccountLookupViewDto;
import bank.front.dto.AccountViewDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountsGatewayClient {

    private final RestClient restClient;

    public AccountsGatewayClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public AccountViewDto getMyAccount() {
        return restClient.get()
                .uri("/api/v1/accounts/me")
                .retrieve()
                .body(AccountViewDto.class);
    }

    public void updateMyAccount(String firstName, String lastName, LocalDate birthDate) {
        restClient.patch()
                .uri("/api/v1/accounts/me")
                .body(Map.of("firstName", firstName, "lastName", lastName, "birthDate", birthDate.toString()))
                .retrieve()
                .toBodilessEntity();
    }

    public List<AccountLookupViewDto> lookup() {
        AccountLookupViewDto[] result = restClient.get()
                .uri("/api/v1/accounts/lookup")
                .retrieve()
                .body(AccountLookupViewDto[].class);
        return result == null ? List.of() : List.of(result);
    }
}
