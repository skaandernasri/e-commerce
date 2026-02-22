ALTER TABLE notification DROP COLUMN IF EXISTS user_id;
ALTER TABLE notification DROP COLUMN IF EXISTS is_read;

CREATE TABLE IF NOT EXISTS user_notification (
    user_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    PRIMARY KEY (user_id, notification_id),
    CONSTRAINT fk_user
      FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification
      FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE CASCADE
);
