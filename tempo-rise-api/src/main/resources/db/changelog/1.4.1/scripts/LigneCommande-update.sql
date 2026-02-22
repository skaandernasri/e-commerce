ALTER TABLE ligne_commande
    DROP COLUMN produit_id,
    ADD COLUMN variant_id BIGINT REFERENCES variant(id) ON DELETE CASCADE;