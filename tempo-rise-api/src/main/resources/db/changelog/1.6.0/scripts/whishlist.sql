CREATE TABLE IF NOT EXISTS whishlist (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_whishlist_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id)
        ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS whishlist_item (
    id BIGSERIAL PRIMARY KEY,
    whishlist_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    added_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_item_whishlist FOREIGN KEY (whishlist_id)
        REFERENCES whishlist (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_item_produit FOREIGN KEY (produit_id)
        REFERENCES produit (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_whishlist_item_unique UNIQUE (whishlist_id, produit_id)
);