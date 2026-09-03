package bank.notifications.controller;

import bank.notifications.dto.NotificationRequestDto;
import bank.notifications.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> notify(@RequestBody NotificationRequestDto dto) {
        notificationService.send(dto.recipientLogin(), dto.message());
        return ResponseEntity.ok().build();
    }
}
