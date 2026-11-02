package bank.cash;

import bank.cash.client.AccountsClient;
import bank.cash.dto.AccountBalanceDto;
import bank.cash.dto.CashOperationResponseDto;
import bank.cash.repository.CashOperationRepository;
import bank.cash.repository.OutboxEventRepository;
import bank.cash.service.CashService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
class CashIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockBean
    AccountsClient accountsClient;

    @Autowired
    CashService cashService;

    @Autowired
    CashOperationRepository cashOperationRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Test
    void deposit_savesOperationAndOutboxEvent() {
        when(accountsClient.credit(eq("ivan"), any(BigDecimal.class)))
                .thenReturn(new AccountBalanceDto(new BigDecimal("150.00")));

        CashOperationResponseDto result = cashService.deposit("ivan", new BigDecimal("100.00"));

        assertTrue(result.success());
        assertEquals(1, cashOperationRepository.count());
        assertEquals(1, outboxEventRepository.count());
    }
}
