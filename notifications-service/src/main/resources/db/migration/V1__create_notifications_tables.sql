CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notification_log (
    id BIGSERIAL PRIMARY KEY,
    recipient_login VARCHAR(255),
    message TEXT,
    created_at TIMESTAMPTZ
);
