CREATE SCHEMA IF NOT EXISTS transfer;

CREATE TABLE transfer.transfer_operations (
    id BIGSERIAL PRIMARY KEY,
    from_login VARCHAR(255) NOT NULL,
    to_login VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ
);

CREATE TABLE transfer.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    recipient_login VARCHAR(255),
    message TEXT,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ
);
