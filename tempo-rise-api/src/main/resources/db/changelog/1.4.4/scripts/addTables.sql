CREATE TABLE IF NOT EXISTS contact (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status_contact VARCHAR(20) NOT NULL,
    refund_method VARCHAR(20),
    is_refunded BOOLEAN,
    refunded_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    commande_id BIGINT references commande(id) ON DELETE CASCADE

);

CREATE TABLE IF NOT EXISTS retour_produit (
    id BIGSERIAL PRIMARY KEY,
    raison_retour TEXT NOT NULL,
    date_retour TIMESTAMP NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);