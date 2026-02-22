
-- Drop foreign key constraint from section
ALTER TABLE section DROP CONSTRAINT IF EXISTS fk_section_page;

-- Drop page_id column from section (optional)
ALTER TABLE section DROP COLUMN IF EXISTS page_id;

-- Drop the page table
DROP TABLE IF EXISTS page CASCADE;

ALTER TABLE section
    ADD COLUMN IF NOT EXISTS type_page VARCHAR(50) NOT NULL,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE section
ADD CONSTRAINT fk_section_created_by
FOREIGN KEY (created_by)
REFERENCES utilisateur(id)
ON DELETE SET NULL;

ALTER TABLE section
ADD CONSTRAINT fk_section_updated_by
FOREIGN KEY (updated_by)
REFERENCES utilisateur(id)
ON DELETE SET NULL;


ALTER TABLE item_group
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT FALSE;

ALTER TABLE item
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT FALSE;
