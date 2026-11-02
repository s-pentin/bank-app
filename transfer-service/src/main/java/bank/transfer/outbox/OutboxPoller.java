package bank.transfer.outbox;

import bank.transfer.client.NotificationsClient;
import bank.transfer.model.OutboxEvent;
import bank.transfer.model.OutboxStatus;
import bank.transfer.repository.OutboxEventRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationsClient notificationsClient;
    private final int maxAttempts;

    public OutboxPoller(OutboxEventRepository outboxEventRepository,
                        NotificationsClient notificationsClient,
                        @Value("${app.outbox.max-attempts:5}") int maxAttempts) {
        this.outboxEventRepository = outboxEventRepository;
        this.notificationsClient = notificationsClient;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:PT5S}")
    @Transactional
    public void poll() {
        List<OutboxEvent> pending = outboxEventRepository
                .findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            try {
                notificationsClient.notify(event.getRecipientLogin(), event.getMessage());
                event.setStatus(OutboxStatus.SENT);
            } catch (Exception e) {
                event.incrementAttempts();
                if (event.getAttempts() >= maxAttempts) {
                    event.setStatus(OutboxStatus.FAILED);
                }
            }
        }
    }
}
