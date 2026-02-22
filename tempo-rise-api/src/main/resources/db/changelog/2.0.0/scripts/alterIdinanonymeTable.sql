ALTER TABLE utilisateuranonyme
ALTER COLUMN id DROP IDENTITY;

ALTER TABLE utilisateuranonyme
ADD CONSTRAINT fk_utilisateuranonyme_utilisateur
FOREIGN KEY (id) REFERENCES utilisateur(id);
