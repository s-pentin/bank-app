package bank.notifications.service;

import bank.notifications.model.NotificationLog;
import bank.notifications.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationLogRepository);
    }

    @Test
    void send_savesLog() {
        notificationService.send("ivan", "Пополнено 100");
        verify(notificationLogRepository).save(any(NotificationLog.class));
    }
}
