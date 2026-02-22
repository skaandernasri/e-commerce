CREATE TABLE IF NOT EXISTS newsletter(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    subscribed_at TIMESTAMP,
    unsubscribed_at TIMESTAMP,
    status VARCHAR(12),
    confirmation_token UUID,
    confirmation_token_expires_at TIMESTAMP
)