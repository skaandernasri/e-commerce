CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    redirect_url VARCHAR(50) NOT NULL,
      CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES utilisateur(id)
        ON DELETE CASCADE
);