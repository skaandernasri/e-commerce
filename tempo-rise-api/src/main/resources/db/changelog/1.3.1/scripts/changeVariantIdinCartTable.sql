-- Step 1: Drop the existing foreign key on produit_id
ALTER TABLE panier_produit DROP CONSTRAINT IF EXISTS panier_produit_produit_id_fkey;

-- Step 2: Drop the existing primary key
ALTER TABLE panier_produit DROP CONSTRAINT IF EXISTS panier_produit_pkey;

-- Step 3: Rename produit_id to variant_id
ALTER TABLE panier_produit RENAME COLUMN produit_id TO variant_id;

-- Step 4: Add the new foreign key to variant(id)
ALTER TABLE panier_produit
    ADD CONSTRAINT panier_produit_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES variant(id) ON DELETE CASCADE;

-- Step 5: Recreate the primary key with panier_id and variant_id
ALTER TABLE panier_produit
    ADD CONSTRAINT panier_produit_pkey PRIMARY KEY (panier_id, variant_id);
