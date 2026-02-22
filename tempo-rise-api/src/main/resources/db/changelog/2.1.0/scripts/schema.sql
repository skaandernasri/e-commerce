CREATE TABLE IF NOT EXISTS configuration_global (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    valeur_livraison NUMERIC(10,2),
    seuil_livraison_gratuite NUMERIC(10,2)
)