ALTER TABLE IF EXISTS contact ADD COLUMN IF NOT EXISTS email VARCHAR(100);

UPDATE contact SET email = utilisateur.email FROM utilisateur WHERE contact.user_id = utilisateur.id;

ALTER TABLE contact ALTER COLUMN email SET NOT NULL;
