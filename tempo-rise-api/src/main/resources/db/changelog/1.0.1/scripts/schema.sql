CREATE TABLE IF NOT EXISTS adresse (
    id BIGINT PRIMARY KEY,
    utilisateur_id BIGINT,
    ligne1 VARCHAR(255),
    ligne2 VARCHAR(255),
    code_postal VARCHAR(10),
    ville VARCHAR(100),
    pays VARCHAR(100),
    type VARCHAR(50),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
 );
ALTER TABLE commande RENAME COLUMN date TO date_commande;

ALTER TABLE commande ALTER COLUMN date_commande SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN statut TYPE VARCHAR(50);

ALTER TABLE commande ADD COLUMN total DECIMAL(10, 2),
    ADD COLUMN adresse_livraison_id BIGINT ,
    ADD COLUMN adresse_facturation_id BIGINT,
    ADD CONSTRAINT fk_adresse_livraison FOREIGN KEY (adresse_livraison_id) REFERENCES adresse(id),
    ADD CONSTRAINT fk_adresse_facturation FOREIGN KEY (adresse_facturation_id) REFERENCES adresse(id);