package bank.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import bank.notifications.model.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
