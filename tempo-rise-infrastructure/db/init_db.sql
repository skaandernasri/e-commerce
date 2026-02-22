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
CREATE TABLE IF NOT EXISTS utilisateur_role (
    user_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    role VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, role)
);
-- Now insert data
INSERT INTO utilisateur (id, nom, prenom, email, isverified, activation_token,
    resetpasswordtoken, resetpasswordexpiresat, activationtokenexpiresat, téléphone, date_inscription, image, genre, date_naissance, loyalty_group)
VALUES
(1, 'Admin', 'Admin', 'skan.nasri@gmail.com', TRUE, NULL, NULL, NULL, NULL, '00000000', '2025-01-01 10:00:00', NULL, 'MALE', '2000-05-01', 0);

INSERT INTO utilisateur_role (user_id,role)
VALUES
(1,'ADMIN');