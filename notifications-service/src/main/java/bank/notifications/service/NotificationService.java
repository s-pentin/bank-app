package bank.notifications.service;

import bank.notifications.model.NotificationLog;
import bank.notifications.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    public void send(String recipientLogin, String message) {
        notificationLogRepository.save(new NotificationLog(recipientLogin, message));
        log.info("Уведомление для {}: {}", recipientLogin, message);
    }
}
