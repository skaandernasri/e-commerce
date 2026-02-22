CREATE TABLE IF NOT EXISTS paiement (
    id BIGSERIAL PRIMARY KEY,
    paiement_ref VARCHAR(50) NOT NULL,
    date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    commande_id BIGINT NOT NULL,
      CONSTRAINT fk_commande
        FOREIGN KEY (commande_id)
        REFERENCES commande(id)
        ON DELETE CASCADE
);