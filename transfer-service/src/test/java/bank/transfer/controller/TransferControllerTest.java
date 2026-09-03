package bank.transfer.controller;

import bank.transfer.config.SecurityConfig;
import bank.transfer.dto.TransferResponseDto;
import bank.transfer.service.TransferService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransferController.class,
        properties = "spring.config.import=optional:consul:")
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    void transfer_withTransferWriteScope_returnsOk() throws Exception {
        when(transferService.transfer(eq("ivan"), eq("petr"), any(BigDecimal.class)))
                .thenReturn(new TransferResponseDto(true, new BigDecimal("50.00"), "Перевод выполнен"));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType("application/json")
                        .content("{\"toLogin\": \"petr\", \"amount\": 100.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_transfer.write"))))
                .andExpect(status().isOk());
    }

    @Test
    void transfer_withoutTransferWriteScope_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType("application/json")
                        .content("{\"toLogin\": \"petr\", \"amount\": 100.00}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_other"))))
                .andExpect(status().isForbidden());
    }
}
