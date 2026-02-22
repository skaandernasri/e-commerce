-- Création de la table utilisateuranonyme si elle n'existe pas (à exécuter sans erreurs si déjà présente)
CREATE TABLE IF NOT EXISTS utilisateuranonyme (
  id BIGINT PRIMARY KEY
);

-- Ajout des colonnes à suivi_client (si elles n'existent pas, tu devras gérer ça dans SQL si possible)
ALTER TABLE suivi_client
  ADD COLUMN IF NOT EXISTS utilisateur_id BIGINT REFERENCES utilisateur(id) ON DELETE CASCADE;

ALTER TABLE suivi_client
  ADD COLUMN IF NOT EXISTS produit_id BIGINT REFERENCES produit(id) ON DELETE CASCADE;

ALTER TABLE suivi_client
  ADD COLUMN IF NOT EXISTS score DOUBLE PRECISION;

-- Renommer colonne action en type_action
-- PostgreSQL ne supporte pas "IF EXISTS" sur rename, donc il faut faire une vérification avant dans SQL ou gérer l’erreur dans liquibase
ALTER TABLE suivi_client RENAME COLUMN action TO type_action;


-- Ajout colonnes à produit
ALTER TABLE produit
  ADD COLUMN IF NOT EXISTS marque VARCHAR(20);

ALTER TABLE produit
  ADD COLUMN IF NOT EXISTS taille VARCHAR(10);

ALTER TABLE produit
  ADD COLUMN IF NOT EXISTS couleur VARCHAR(20);

-- Ajout colonnes à utilisateuranonyme
ALTER TABLE utilisateuranonyme
  ADD COLUMN IF NOT EXISTS session_token UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE utilisateuranonyme
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Ajout colonne utilisateur_anonyme_id
ALTER TABLE suivi_client
  ADD COLUMN IF NOT EXISTS utilisateur_anonyme_id BIGINT REFERENCES utilisateuranonyme(id) ON DELETE CASCADE;

-- Ajout contrainte CHECK (attention: pas possible en SQL standard de vérifier si elle existe, il faut la gérer dans liquibase)
ALTER TABLE suivi_client
  ADD CONSTRAINT utilisateur_or_anonymous_check CHECK (
    (utilisateur_id IS NOT NULL AND utilisateur_anonyme_id IS NULL)
    OR (utilisateur_id IS NULL AND utilisateur_anonyme_id IS NOT NULL)
  );

-- Suppression contrainte NOT NULL sur certaines colonnes (si elles existent)
ALTER TABLE suivi_client ALTER COLUMN utilisateur_id DROP NOT NULL;
ALTER TABLE suivi_client ALTER COLUMN utilisateur_anonyme_id DROP NOT NULL;

-- Modifier type colonne date
ALTER TABLE suivi_client ALTER COLUMN date TYPE TIMESTAMP WITHOUT TIME ZONE USING date AT TIME ZONE 'UTC';

-- Ajout auto-increment sur utilisateuranonyme.id (PostgreSQL : SERIAL ou IDENTITY)
ALTER TABLE utilisateuranonyme ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;
