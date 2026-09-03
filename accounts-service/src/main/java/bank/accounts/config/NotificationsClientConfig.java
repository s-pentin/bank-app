package bank.accounts.config;

import bank.accounts.client.NotificationsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificationsClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(OAuth2AuthorizedClientManager manager) {
        return RestClient.builder().requestInterceptor(m2mInterceptor(manager));
    }

    @Bean
    public NotificationsClient notificationsClient(@LoadBalanced RestClient.Builder builder,
                                                   @Value("${app.notifications.base-url}") String baseUrl) {
        return new NotificationsClient(builder.baseUrl(baseUrl).build());
    }

    private ClientHttpRequestInterceptor m2mInterceptor(OAuth2AuthorizedClientManager manager) {
        return (request, body, execution) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("notifications-client")
                    .principal(new AnonymousAuthenticationToken("bank", "accounts-service",
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
                    .build();
            OAuth2AuthorizedClient client = manager.authorize(authorizeRequest);
            if (client == null) {
                throw new IllegalStateException("Не удалось получить M2M-токен для notifications-client");
            }
            request.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
            return execution.execute(request, body);
        };
    }
}
