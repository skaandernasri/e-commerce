-- schema.sql

-- Enum Types (already defined in previous schema)
--CREATE TYPE role AS ENUM ('CLIENT', 'GESTIONNAIRE', 'ADMIN', 'REDACTEUR', 'SUPER_ADMIN');
--CREATE TYPE type_authentification AS ENUM ('EMAIL', 'FACEBOOK', 'GOOGLE');
--CREATE TYPE statut_commande AS ENUM ('EN_COURS', 'EXPEDIEE', 'LIVREE');
--CREATE TYPE mode_paiement AS ENUM ('CARTE_BANCAIRE', 'VIREMENT', 'A_LIVRAISON');

-- Table: utilisateur (updated to include roles as a separate table)
CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(25),
    prenom VARCHAR(100),
    email VARCHAR(50) NOT NULL UNIQUE,
    isverified BOOLEAN DEFAULT FALSE,
    activation_token TEXT,
    resetpasswordtoken TEXT,
    resetpasswordexpiresat TIMESTAMP WITH TIME ZONE ,
    activationtokenexpiresat TIMESTAMP WITH TIME ZONE,
    téléphone VARCHAR(20),
    date_inscription TIMESTAMP WITH TIME ZONE,
    image bytea,
    genre VARCHAR(10),
    date_naissance DATE,
    loyalty_group INT DEFAULT 0
);


-- Table: utilisateur_role (for the many-to-many relationship between utilisateur and roles)
CREATE TABLE IF NOT EXISTS utilisateur_role (
    user_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    role VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Table: authentification (already defined in previous schema)
CREATE TABLE IF NOT EXISTS authentification (
    id BIGSERIAL PRIMARY KEY,
    password VARCHAR(100),
    provider_id VARCHAR(2),
    type VARCHAR(10) NOT NULL,
    refresh_token TEXT,
    user_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);



-- Table: blogpost (already defined in previous schema)
CREATE TABLE IF NOT EXISTS blogpost (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(50) NOT NULL,
    contenu TEXT NOT NULL,
    date_publication DATE NOT NULL,
    auteur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- Table: commentaire (already defined in previous schema)
CREATE TABLE IF NOT EXISTS commentaire (
    id BIGSERIAL PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_publication DATE NOT NULL,
    auteur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    blogpost_id BIGINT NOT NULL REFERENCES blogpost(id) ON DELETE CASCADE
);

-- Table: image_blogpost (already defined in previous schema)
CREATE TABLE IF NOT EXISTS image_blogpost (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(60) NOT NULL,
    blogpost_id BIGINT NOT NULL REFERENCES blogpost(id) ON DELETE CASCADE
);

-- Table: categorie (already defined in previous schema)
CREATE TABLE IF NOT EXISTS categorie (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL,
    description TEXT
);

-- Table: produit (updated to include stock and relationships)
CREATE TABLE IF NOT EXISTS produit (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL,
    description TEXT,
    prix DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    categorie_id BIGINT REFERENCES categorie(id) ON DELETE CASCADE
    -- it was     categorie_id BIGINT NOT NULL REFERENCES categorie(id) ON DELETE CASCADE

);
-- Table: panier
CREATE TABLE panier (
    id BIGSERIAL PRIMARY KEY ,
    utilisateur_id BIGINT NOT NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
);
-- Table: panier_produit
CREATE TABLE Panier_Produit (
    panier_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    PRIMARY KEY (panier_id, produit_id),
    FOREIGN KEY (panier_id) REFERENCES Panier(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE CASCADE
);

-- Table: avis (already defined in previous schema)
CREATE TABLE IF NOT EXISTS avis (
    id BIGSERIAL PRIMARY KEY,
    note INT NOT NULL,
    commentaire TEXT,
    date_publication TIMESTAMP NOT NULL,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE
);
-- Table: promotion
CREATE TABLE IF NOT EXISTS promotion (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(20),
    description TEXT,
    pourcentage_reduction DECIMAL(5, 2) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE
);

-- Table: image_produit
CREATE TABLE IF NOT EXISTS image_produit (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(60) NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE
);

-- Table: codepromo (already defined in previous schema)
CREATE TABLE IF NOT EXISTS codepromo (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    reduction DECIMAL(5, 2) NOT NULL,
    date_expiration TIMESTAMP NOT NULL
);

-- Table: commande (already defined in previous schema)
CREATE TABLE IF NOT EXISTS commande (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    statut VARCHAR(10) NOT NULL,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    mode_paiement VARCHAR(10) NOT NULL,
    code_promo_id BIGINT REFERENCES codepromo(id) ON DELETE SET NULL
);

-- Table: ligne_commande (updated to include prixTotal)
CREATE TABLE IF NOT EXISTS ligne_commande (
    id BIGSERIAL PRIMARY KEY,
    quantite INT NOT NULL,
    prix_total DECIMAL(10, 2) NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
    commande_id BIGINT NOT NULL REFERENCES commande(id) ON DELETE CASCADE
);

-- Table: facture (already defined in previous schema)
CREATE TABLE IF NOT EXISTS facture (
    id BIGSERIAL PRIMARY KEY,
    commande_id BIGINT NOT NULL REFERENCES commande(id) ON DELETE CASCADE,
    date_emission TIMESTAMP NOT NULL,
    total DECIMAL(10, 2) NOT NULL
);

-- Table: historique_commande
CREATE TABLE IF NOT EXISTS historique_commande (
    id BIGSERIAL PRIMARY KEY,
    date_commande TIMESTAMP NOT NULL,
    statut VARCHAR(50) NOT NULL,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);
-- Table: historiqueCommande_produit
CREATE TABLE historiqueCommande_produit (
    historiqueCommande_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    PRIMARY KEY (historiqueCommande_id, produit_id),
    FOREIGN KEY (historiqueCommande_id) REFERENCES historique_commande(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE CASCADE
);

-- Table: retour_produit
CREATE TABLE IF NOT EXISTS retour_produit (
    id BIGSERIAL PRIMARY KEY,
    raison_retour VARCHAR(100) NOT NULL,
    date_retour TIMESTAMP NOT NULL,
    produit_id BIGINT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- Table: suivi_client
CREATE TABLE IF NOT EXISTS suivi_client (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(20) NOT NULL,
    date TIMESTAMP NOT NULL,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE
);