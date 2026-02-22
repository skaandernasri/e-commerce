ALTER TABLE adresse ALTER COLUMN id DROP DEFAULT;
            ALTER TABLE adresse ALTER COLUMN id TYPE BIGINT;
            CREATE SEQUENCE IF NOT EXISTS adresse_id_seq START WITH 1 INCREMENT BY 1;
            ALTER TABLE adresse ALTER COLUMN id SET DEFAULT nextval('adresse_id_seq');