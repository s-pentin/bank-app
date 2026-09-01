package bank.front.controller;

import bank.front.client.AccountsGatewayClient;
import bank.front.client.CashGatewayClient;
import bank.front.client.TransferGatewayClient;
import bank.front.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MainController.class,
        properties = "spring.config.import=optional:consul:")
@Import(SecurityConfig.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsGatewayClient accountsGatewayClient;

    @MockBean
    private CashGatewayClient cashGatewayClient;

    @MockBean
    private TransferGatewayClient transferGatewayClient;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void index_withoutSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
