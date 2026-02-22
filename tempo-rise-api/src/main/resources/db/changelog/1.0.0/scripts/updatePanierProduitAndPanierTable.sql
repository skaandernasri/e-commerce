ALTER TABLE panier
ADD CONSTRAINT unique_utilisateur_id UNIQUE (utilisateur_id);

ALTER TABLE panier_produit
ADD COLUMN expiration_date timestamp;