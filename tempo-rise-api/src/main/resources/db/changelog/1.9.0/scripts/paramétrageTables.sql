-- Create page table
CREATE TABLE IF NOT EXISTS page (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Stores enum as string
    meta_title VARCHAR(255),
    meta_description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Create section table
CREATE TABLE IF NOT EXISTS section (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Stores enum as string
    ordre INTEGER NOT NULL,
    contenu_html TEXT,
    page_id BIGINT NOT NULL,
    CONSTRAINT fk_section_page FOREIGN KEY (page_id) REFERENCES page(id) ON DELETE CASCADE
);

-- Create item_group table
CREATE TABLE IF NOT EXISTS item_group (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    ordre INTEGER NOT NULL,
    section_id BIGINT NOT NULL,
    CONSTRAINT fk_item_group_section FOREIGN KEY (section_id) REFERENCES section(id) ON DELETE CASCADE
);

-- Create item table
CREATE TABLE IF NOT EXISTS item (
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    valeur TEXT,
    type VARCHAR(50) NOT NULL, -- Stores enum as string
    ordre INTEGER NOT NULL,
    section_id BIGINT,
    item_group_id BIGINT,
    CONSTRAINT fk_item_section FOREIGN KEY (section_id) REFERENCES section(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_item_group FOREIGN KEY (item_group_id) REFERENCES item_group(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_page_slug ON page(slug);
CREATE INDEX idx_page_active ON page(is_active);
CREATE INDEX idx_section_page_id ON section(page_id);
CREATE INDEX idx_section_ordre ON section(ordre);
CREATE INDEX idx_item_group_section_id ON item_group(section_id);
CREATE INDEX idx_item_section_id ON item(section_id);
CREATE INDEX idx_item_item_group_id ON item(item_group_id);
CREATE INDEX idx_item_ordre ON item(ordre);