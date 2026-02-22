CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(25),
    prenom VARCHAR(100),
    email VARCHAR(50) NOT NULL UNIQUE,
    isverified BOOLEAN DEFAULT FALSE,
    activation_token TEXT,
    resetpasswordtoken TEXT,
    resetpasswordexpiresat TIMESTAMP  ,
    activationtokenexpiresat TIMESTAMP ,
    téléphone VARCHAR(20),
    date_inscription TIMESTAMP ,
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
(1, 'Admin', 'Admin', 'admin@tempo.tn', TRUE, NULL, NULL, NULL, NULL, '00000000', '2025-01-01 10:00:00', NULL, 'MALE', '2000-05-01', 0);

INSERT INTO utilisateur_role (user_id,role)
VALUES
(1,'ADMIN');
INSERT INTO authentification (password, provider_id, type, refresh_token, user_id)
VALUES
('$2a$10$LIt1ZzjjdICvi071mRX21u0a5U5UZ/570PIwTcC.uJpprXKij7.ci','0','EMAIL',NULL,1)
-- password is Admin123.