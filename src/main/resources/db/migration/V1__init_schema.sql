-- =====================================================================
-- V1 : schema initial - Gestion comptable et suivi des aides (MVP)
-- =====================================================================

CREATE TABLE utilisateur (
    id                  BIGSERIAL PRIMARY KEY,
    nom                 VARCHAR(200)    NOT NULL,
    identifiant         VARCHAR(100)    NOT NULL UNIQUE,
    mot_de_passe_hash   VARCHAR(255)    NOT NULL,
    role                VARCHAR(30)     NOT NULL CHECK (role IN ('COMPTABLE', 'DIRECTEUR')),
    actif               BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE financement (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)     NOT NULL UNIQUE,
    libelle     VARCHAR(255)    NOT NULL,
    bailleur    VARCHAR(255),
    date_debut  DATE            NOT NULL,
    date_fin    DATE,
    statut      VARCHAR(30)     NOT NULL CHECK (statut IN ('ACTIF', 'CLOTURE')),
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE compte (
    id              BIGSERIAL PRIMARY KEY,
    numero          VARCHAR(20)     NOT NULL UNIQUE,
    libelle         VARCHAR(255)    NOT NULL,
    est_tresorerie  BOOLEAN         NOT NULL DEFAULT FALSE,
    projet_dedie_id BIGINT          REFERENCES financement(id),
    actif           BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE destination (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(30)     NOT NULL UNIQUE,
    libelle VARCHAR(255)    NOT NULL,
    actif   BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE ligne_budget (
    id              BIGSERIAL PRIMARY KEY,
    financement_id  BIGINT          NOT NULL REFERENCES financement(id),
    libelle         VARCHAR(255)    NOT NULL,
    montant         NUMERIC(14,2)   NOT NULL CHECK (montant >= 0),
    exercice        INTEGER         NOT NULL
);

CREATE TABLE ecriture (
    id                  BIGSERIAL PRIMARY KEY,
    numero              INTEGER,
    perimetre_type      VARCHAR(20)     NOT NULL CHECK (perimetre_type IN ('GENERAL', 'PROJET')),
    financement_id      BIGINT          REFERENCES financement(id),
    date_operation      DATE            NOT NULL,
    libelle             VARCHAR(255)    NOT NULL,
    reference_piece     VARCHAR(255)    NOT NULL,
    type_operation      VARCHAR(30)     NOT NULL CHECK (type_operation IN
                            ('ENCAISSEMENT', 'DECAISSEMENT', 'TRANSFERT', 'OPERATION_DIVERSE', 'CONTREPASSATION', 'PAIEMENT_AIDE')),
    statut              VARCHAR(20)     NOT NULL CHECK (statut IN ('BROUILLON', 'VALIDEE', 'CONTREPASSEE')),
    ecriture_origine_id BIGINT          REFERENCES ecriture(id),
    cree_par            VARCHAR(100)    NOT NULL,
    valide_par          VARCHAR(100),
    cree_le             TIMESTAMP       NOT NULL,
    valide_le           TIMESTAMP,
    version             BIGINT          NOT NULL DEFAULT 0,
    CHECK (perimetre_type = 'GENERAL' OR financement_id IS NOT NULL),
    CHECK (perimetre_type = 'PROJET' OR financement_id IS NULL)
);

-- Numerotation sequentielle sans trou par perimetre : deux index partiels
-- car financement_id est NULL pour le perimetre GENERAL (NULL != NULL en unicite standard).
CREATE UNIQUE INDEX uq_ecriture_numero_general ON ecriture(perimetre_type, numero)
    WHERE financement_id IS NULL AND numero IS NOT NULL;
CREATE UNIQUE INDEX uq_ecriture_numero_projet ON ecriture(perimetre_type, financement_id, numero)
    WHERE financement_id IS NOT NULL AND numero IS NOT NULL;

CREATE INDEX idx_ecriture_date ON ecriture(date_operation);
CREATE INDEX idx_ecriture_financement ON ecriture(financement_id);
CREATE INDEX idx_ecriture_statut ON ecriture(statut);

CREATE TABLE ligne_ecriture (
    id              BIGSERIAL PRIMARY KEY,
    ecriture_id     BIGINT          NOT NULL REFERENCES ecriture(id) ON DELETE CASCADE,
    compte_id       BIGINT          NOT NULL REFERENCES compte(id),
    debit           NUMERIC(14,2)   NOT NULL DEFAULT 0 CHECK (debit >= 0),
    credit          NUMERIC(14,2)   NOT NULL DEFAULT 0 CHECK (credit >= 0),
    destination_id  BIGINT          REFERENCES destination(id),
    ligne_budget_id BIGINT          REFERENCES ligne_budget(id),
    pointe          BOOLEAN         NOT NULL DEFAULT FALSE,
    CHECK (NOT (debit > 0 AND credit > 0)),
    CHECK (debit + credit > 0)
);

CREATE INDEX idx_ligne_ecriture_ecriture ON ligne_ecriture(ecriture_id);
CREATE INDEX idx_ligne_ecriture_compte ON ligne_ecriture(compte_id);

CREATE TABLE compteur_ecriture (
    id              BIGSERIAL PRIMARY KEY,
    perimetre_type  VARCHAR(20)     NOT NULL CHECK (perimetre_type IN ('GENERAL', 'PROJET')),
    financement_id  BIGINT          REFERENCES financement(id),
    dernier_numero  INTEGER         NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_compteur_general ON compteur_ecriture(perimetre_type)
    WHERE financement_id IS NULL;
CREATE UNIQUE INDEX uq_compteur_projet ON compteur_ecriture(perimetre_type, financement_id)
    WHERE financement_id IS NOT NULL;

CREATE TABLE periode_cloturee (
    id          BIGSERIAL PRIMARY KEY,
    annee       INTEGER         NOT NULL,
    mois        INTEGER         NOT NULL CHECK (mois BETWEEN 1 AND 12),
    cloture_par VARCHAR(100)    NOT NULL,
    cloture_le  TIMESTAMP       NOT NULL,
    UNIQUE (annee, mois)
);

CREATE TABLE tuteur (
    id                      BIGSERIAL PRIMARY KEY,
    nom                     VARCHAR(255)    NOT NULL,
    piece_identite_type     VARCHAR(50),
    piece_identite_numero   VARCHAR(100),
    telephone               VARCHAR(30)
);

CREATE TABLE enfant (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(255)    NOT NULL,
    date_naissance  DATE,
    sexe            VARCHAR(10),
    tuteur_id       BIGINT          NOT NULL REFERENCES tuteur(id),
    date_entree     DATE            NOT NULL,
    statut          VARCHAR(20)     NOT NULL CHECK (statut IN ('ACTIF', 'SORTI')),
    notes           TEXT
);

CREATE TABLE aide (
    id                  BIGSERIAL PRIMARY KEY,
    enfant_id           BIGINT          NOT NULL REFERENCES enfant(id),
    montant_mensuel     NUMERIC(14,2)   NOT NULL CHECK (montant_mensuel >= 0),
    financement_id      BIGINT          NOT NULL REFERENCES financement(id),
    reference_decision  VARCHAR(255)    NOT NULL,
    date_debut          DATE            NOT NULL,
    date_fin            DATE,
    statut              VARCHAR(20)     NOT NULL CHECK (statut IN ('ACTIVE', 'SUSPENDUE', 'CLOTUREE')),
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE INDEX idx_aide_enfant ON aide(enfant_id);
CREATE INDEX idx_aide_financement ON aide(financement_id);

CREATE TABLE liste_paiement (
    id              BIGSERIAL PRIMARY KEY,
    annee           INTEGER         NOT NULL,
    mois            INTEGER         NOT NULL CHECK (mois BETWEEN 1 AND 12),
    statut          VARCHAR(20)     NOT NULL CHECK (statut IN ('GENEREE', 'VALIDEE', 'CLOTUREE')),
    generee_par     VARCHAR(100)    NOT NULL,
    validee_par     VARCHAR(100),
    cloturee_par    VARCHAR(100),
    UNIQUE (annee, mois)
);

CREATE TABLE ligne_paiement (
    id                      BIGSERIAL PRIMARY KEY,
    liste_id                BIGINT          NOT NULL REFERENCES liste_paiement(id) ON DELETE CASCADE,
    aide_id                 BIGINT          NOT NULL REFERENCES aide(id),
    montant                 NUMERIC(14,2)   NOT NULL CHECK (montant >= 0),
    statut                  VARCHAR(20)     NOT NULL CHECK (statut IN ('A_PAYER', 'VERSE', 'NON_VERSE')),
    date_versement          DATE,
    motif_non_versement     VARCHAR(500),
    reference_decharge      VARCHAR(255),
    ecriture_id             BIGINT          REFERENCES ecriture(id)
);

CREATE INDEX idx_ligne_paiement_liste ON ligne_paiement(liste_id);

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    utilisateur VARCHAR(100)    NOT NULL,
    action      VARCHAR(100)    NOT NULL,
    entite      VARCHAR(100)    NOT NULL,
    entite_id   BIGINT,
    details     TEXT,
    horodatage  TIMESTAMP       NOT NULL
);

CREATE INDEX idx_audit_log_entite ON audit_log(entite, entite_id);
