ALTER TABLE adresse DROP CONSTRAINT adresse_utilisateur_id_fkey;

-- 3. Add a new foreign key constraint with ON DELETE CASCADE
ALTER TABLE adresse
ADD CONSTRAINT adresse_utilisateur_id_fkey
FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
ON DELETE CASCADE;