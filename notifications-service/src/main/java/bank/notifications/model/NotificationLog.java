package bank.notifications.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "notification_log", schema = "notifications")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_login")
    private String recipientLogin;

    private String message;

    @Column(name = "created_at")
    private Instant createdAt;

    protected NotificationLog() {
    }

    public NotificationLog(String recipientLogin, String message) {
        this.recipientLogin = recipientLogin;
        this.message = message;
        this.createdAt = Instant.now();
    }

}
