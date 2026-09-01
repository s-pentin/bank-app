package bank.accounts.controller;

import bank.accounts.config.SecurityConfig;
import bank.accounts.dto.AccountResponseDto;
import bank.accounts.service.AccountService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountController.class, InternalAccountController.class},
        properties = "spring.config.import=optional:consul:")
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void getMyAccount_withReadScope_returnsOwnAccount() throws Exception {
        when(accountService.getMyAccount("ivan")).thenReturn(new AccountResponseDto(
                "ivan", "Ivan", "Ivanov", LocalDate.of(1990, 1, 1), new BigDecimal("100.00")));

        mockMvc.perform(get("/api/v1/accounts/me")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_accounts.read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("ivan"));
    }

    @Test
    void getMyAccount_withoutReadScope_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/me")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_other"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_withoutInternalScope_returns403() throws Exception {
        mockMvc.perform(post("/internal/accounts/ivan/debit")
                        .contentType("application/json")
                        .content("{\"amount\": 10.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_accounts.read"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_withInternalScope_returnsOk() throws Exception {
        when(accountService.debit("ivan", new BigDecimal("10.00"))).thenReturn(new AccountResponseDto(
                "ivan", "Ivan", "Ivanov", LocalDate.of(1990, 1, 1), new BigDecimal("90.00")));

        mockMvc.perform(post("/internal/accounts/ivan/debit")
                        .contentType("application/json")
                        .content("{\"amount\": 10.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_accounts.internal"))))
                .andExpect(status().isOk());
    }
}
