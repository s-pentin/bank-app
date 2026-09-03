package bank.accounts.outbox;

import bank.accounts.client.NotificationsClient;
import bank.accounts.model.OutboxEvent;
import bank.accounts.model.OutboxStatus;
import bank.accounts.repository.OutboxEventRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    private static final int MAX_ATTEMPTS = 5;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private NotificationsClient notificationsClient;

    private OutboxPoller outboxPoller;

    @BeforeEach
    void setUp() {
        outboxPoller = new OutboxPoller(outboxEventRepository, notificationsClient, MAX_ATTEMPTS);
    }

    @Test
    void poll_marksEventSentOnSuccess() {
        OutboxEvent event = new OutboxEvent("ivan", "Списано 100");
        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(event));

        outboxPoller.poll();

        assertEquals(OutboxStatus.SENT, event.getStatus());
    }

    @Test
    void poll_keepsPendingAndIncrementsAttemptsOnFailure() {
        OutboxEvent event = new OutboxEvent("ivan", "Списано 100");
        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("down"))
                .when(notificationsClient).notify(anyString(), anyString());

        outboxPoller.poll();

        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals(1, event.getAttempts());
    }

    @Test
    void poll_marksFailedAfterMaxAttempts() {
        OutboxEvent event = new OutboxEvent("ivan", "Списано 100");
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            event.incrementAttempts();
        }
        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("down"))
                .when(notificationsClient).notify(anyString(), anyString());

        outboxPoller.poll();

        assertEquals(OutboxStatus.FAILED, event.getStatus());
        assertEquals(MAX_ATTEMPTS, event.getAttempts());
    }
}
