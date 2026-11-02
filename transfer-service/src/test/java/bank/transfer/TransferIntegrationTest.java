package bank.transfer;

import bank.transfer.client.AccountsClient;
import bank.transfer.dto.AccountBalanceDto;
import bank.transfer.dto.TransferResponseDto;
import bank.transfer.repository.OutboxEventRepository;
import bank.transfer.repository.TransferOperationRepository;
import bank.transfer.service.TransferService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
class TransferIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    AccountsClient accountsClient;

    @Autowired
    TransferService transferService;

    @Autowired
    TransferOperationRepository transferOperationRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Test
    void transfer_savesOperationAndOutboxEvent() {
        when(accountsClient.debit(eq("ivan"), any(BigDecimal.class)))
                .thenReturn(new AccountBalanceDto(new BigDecimal("50.00")));
        when(accountsClient.credit(eq("petr"), any(BigDecimal.class)))
                .thenReturn(new AccountBalanceDto(new BigDecimal("200.00")));

        TransferResponseDto result = transferService.transfer("ivan", "petr", new BigDecimal("100.00"));

        assertTrue(result.success());
        assertEquals(1, transferOperationRepository.count());
        assertEquals(1, outboxEventRepository.count());
    }
}
