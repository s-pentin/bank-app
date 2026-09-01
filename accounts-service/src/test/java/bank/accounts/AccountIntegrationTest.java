package bank.accounts;

import bank.accounts.dto.AccountResponseDto;
import bank.accounts.model.OutboxEvent;
import bank.accounts.model.OutboxStatus;
import bank.accounts.repository.OutboxEventRepository;
import bank.accounts.service.AccountService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
class AccountIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockBean
    JwtDecoder jwtDecoder;

    @Autowired
    AccountService accountService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Test
    void fullCycle_createsAccountDebitsAndWritesOutbox() {
        AccountResponseDto created = accountService.getMyAccount("ivan");
        assertEquals(0, created.balance().compareTo(BigDecimal.ZERO));

        AccountResponseDto credited = accountService.credit("ivan", new BigDecimal("100.00"));
        assertEquals(0, credited.balance().compareTo(new BigDecimal("100.00")));

        AccountResponseDto debited = accountService.debit("ivan", new BigDecimal("30.00"));
        assertEquals(0, debited.balance().compareTo(new BigDecimal("70.00")));

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertEquals(2, events.size());
        for (OutboxEvent event : events) {
            assertEquals(OutboxStatus.PENDING, event.getStatus());
        }
    }
}
