package bank.cash.controller;

import bank.cash.config.SecurityConfig;
import bank.cash.dto.CashOperationResponseDto;
import bank.cash.service.CashService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CashController.class,
        properties = "spring.config.import=optional:consul:")
@Import(SecurityConfig.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashService cashService;

    @Test
    void deposit_withCashWriteScope_returnsOk() throws Exception {
        when(cashService.deposit(eq("ivan"), any(BigDecimal.class)))
                .thenReturn(new CashOperationResponseDto(true, new BigDecimal("150.00"), "Пополнение выполнено"));

        mockMvc.perform(post("/api/v1/cash/deposit")
                        .contentType("application/json")
                        .content("{\"amount\": 100.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_cash.write"))))
                .andExpect(status().isOk());
    }

    @Test
    void deposit_withoutCashWriteScope_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/cash/deposit")
                        .contentType("application/json")
                        .content("{\"amount\": 100.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_other"))))
                .andExpect(status().isForbidden());
    }
}
