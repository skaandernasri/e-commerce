ALTER TABLE newsletter RENAME TO news_letter;
ALTER TABLE IF EXISTS news_letter ADD COLUMN unsubscription_token UUID;
ALTER TABLE news_letter ADD COLUMN unsubscription_token_expires_at TIMESTAMP;
