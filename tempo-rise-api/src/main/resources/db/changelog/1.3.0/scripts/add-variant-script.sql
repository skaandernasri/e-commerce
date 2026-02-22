CREATE TABLE IF NOT EXISTS variant (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
  color VARCHAR(100) NOT NULL,
  size VARCHAR(50) NOT NULL,
  quantity INTEGER DEFAULT 0 CHECK (quantity >= 0)
);


ALTER TABLE variant
ADD CONSTRAINT unique_variant_per_product
UNIQUE (product_id, color, size);
