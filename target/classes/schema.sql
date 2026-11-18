CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    niveau_hierarchique INTEGER NOT NULL DEFAULT 0 CHECK (niveau_hierarchique >= 0),
    role VARCHAR(24) NOT NULL CHECK (role IN ('UTILISATEUR', 'AUTORITE', 'ADMINISTRATEUR'))
);

CREATE TABLE IF NOT EXISTS salle (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL UNIQUE,
    capacite INTEGER NOT NULL CHECK (capacite > 0),
    equipements TEXT NOT NULL DEFAULT '',
    statut VARCHAR(16) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (statut IN ('DISPONIBLE', 'OCCUPEE'))
);

ALTER TABLE salle DROP CONSTRAINT IF EXISTS salle_statut_check;
ALTER TABLE salle DROP CONSTRAINT IF EXISTS salle_statut_valid;
ALTER TABLE salle ADD CONSTRAINT salle_statut_valid
    CHECK (statut IN ('DISPONIBLE', 'RESERVEE', 'OCCUPEE'));

CREATE TABLE IF NOT EXISTS reservation (
    id BIGSERIAL PRIMARY KEY,
    salle_id BIGINT NOT NULL REFERENCES salle(id),
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id),
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    motif VARCHAR(500) NOT NULL,
    statut VARCHAR(32) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (statut IN ('EN_ATTENTE', 'CONFIRMEE', 'EN_ATTENTE_PREEMPTION',
            'REDUITE', 'LIBEREE', 'ANNULEE')),
    prioritaire BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT reservation_dates_valides CHECK (date_fin > date_debut)
);

CREATE INDEX IF NOT EXISTS reservation_salle_periode_idx
    ON reservation(salle_id, date_debut, date_fin);
CREATE INDEX IF NOT EXISTS reservation_utilisateur_idx ON reservation(utilisateur_id);

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES reservation(id),
    destinataire_id BIGINT NOT NULL REFERENCES utilisateur(id),
    message TEXT NOT NULL,
    delai_minutes INTEGER NOT NULL,
    date_envoi TIMESTAMP NOT NULL,
    demande_reservation_id BIGINT NOT NULL REFERENCES reservation(id),
    date_echeance TIMESTAMP NOT NULL,
    traitee BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_delai_minutes_check;
ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_delai_minutes_range;
ALTER TABLE notification ADD CONSTRAINT notification_delai_minutes_range
    CHECK (delai_minutes BETWEEN 1 AND 1440);

CREATE INDEX IF NOT EXISTS notification_echeance_idx
    ON notification(date_echeance) WHERE traitee = FALSE;
CREATE INDEX IF NOT EXISTS notification_destinataire_idx
    ON notification(destinataire_id, date_envoi DESC);

CREATE TABLE IF NOT EXISTS parametre_application (
    cle VARCHAR(64) PRIMARY KEY,
    valeur VARCHAR(255) NOT NULL
);

INSERT INTO parametre_application (cle, valeur)
VALUES
    ('preemption.delai_niveau_max', '60'),
    ('preemption.delai_autres', '30'),
    ('preemption.seuil_liberation', '60')
ON CONFLICT (cle) DO NOTHING;

INSERT INTO salle (nom, capacite, equipements)
VALUES
    ('Salle A', 8, U&'\00C9cran, visioconf\00E9rence'),
    ('Salle B', 12, U&'\00C9cran, tableau blanc'),
    ('Salle C', 20, U&'Vid\00E9oprojecteur, visioconf\00E9rence')
ON CONFLICT (nom) DO NOTHING;

UPDATE salle
SET equipements = U&'\00C9cran, visioconf\00E9rence'
WHERE nom = 'Salle A'
  AND equipements = U&'\00C3\2030cran, visioconf\00C3\00A9rence';

UPDATE salle
SET equipements = U&'\00C9cran, tableau blanc'
WHERE nom = 'Salle B'
  AND equipements = U&'\00C3\2030cran, tableau blanc';

UPDATE salle
SET equipements = U&'Vid\00E9oprojecteur, visioconf\00E9rence'
WHERE nom = 'Salle C'
  AND equipements = U&'Vid\00C3\00A9oprojecteur, visioconf\00C3\00A9rence';

UPDATE salle s
SET statut = CASE
    WHEN EXISTS (
        SELECT 1 FROM reservation r
        WHERE r.salle_id = s.id
          AND r.date_debut <= CURRENT_TIMESTAMP
          AND r.date_fin > CURRENT_TIMESTAMP
          AND r.statut IN ('CONFIRMEE', 'REDUITE')
    ) THEN 'OCCUPEE'
    WHEN EXISTS (
        SELECT 1 FROM reservation r
        WHERE r.salle_id = s.id
          AND r.date_debut > CURRENT_TIMESTAMP
          AND r.statut IN ('CONFIRMEE', 'REDUITE', 'EN_ATTENTE_PREEMPTION')
    ) THEN 'RESERVEE'
    ELSE 'DISPONIBLE'
END;
