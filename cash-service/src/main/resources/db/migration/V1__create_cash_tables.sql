CREATE SCHEMA IF NOT EXISTS cash;

CREATE TABLE cash.cash_operations (
    id BIGSERIAL PRIMARY KEY,
    user_login VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ
);

CREATE TABLE cash.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    recipient_login VARCHAR(255),
    message TEXT,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ
);
