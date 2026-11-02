CREATE SCHEMA IF NOT EXISTS accounts;

CREATE TABLE accounts.accounts (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    birth_date DATE,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ
);

CREATE TABLE accounts.outbox_events (
    id BIGSERIAL PRIMARY KEY,
    recipient_login VARCHAR(255),
    message TEXT,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ
);
