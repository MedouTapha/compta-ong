-- Repeatable Flyway migration : seed data for development / testing
-- Re-runs whenever content changes (checksum-based).

-- Utilisateurs (mot de passe = BCrypt de 'password')
INSERT INTO utilisateur (nom, identifiant, mot_de_passe_hash, role, actif)
VALUES
    ('Directeur Test', 'directeur', '$2a$10$dXJ3SW6G7P50lGmMQgel2e1DFWojD.PyH3KFzH0Hv.CjZdLlHCWG.', 'DIRECTEUR', true),
    ('Comptable Test', 'comptable', '$2a$10$dXJ3SW6G7P50lGmMQgel2e1DFWojD.PyH3KFzH0Hv.CjZdLlHCWG.', 'COMPTABLE', true)
ON CONFLICT (identifiant) DO NOTHING;

-- Financements
INSERT INTO financement (code, libelle, bailleur, date_debut, date_fin, statut)
VALUES
    ('UNICEF-2026', 'Programme sante enfants', 'UNICEF', '2026-01-01', '2026-12-31', 'ACTIF'),
    ('PAM-NUTRI', 'Nutrition communautaire', 'PAM', '2026-01-01', NULL, 'ACTIF')
ON CONFLICT (code) DO NOTHING;

-- Comptes
INSERT INTO compte (numero, libelle, est_tresorerie, projet_dedie_id, actif)
VALUES
    ('5711', 'Caisse principale', true, NULL, true),
    ('5211', 'Banque compte courant', true, NULL, true),
    ('6011', 'Achats fournitures', false, NULL, true),
    ('6031', 'Achats vivres', false, NULL, true),
    ('6511', 'Aides aux beneficiaires', false, NULL, true),
    ('7411', 'Subventions recues', false, NULL, true),
    ('7412', 'Dons recus', false, NULL, true)
ON CONFLICT (numero) DO NOTHING;

-- Destinations
INSERT INTO destination (code, libelle, actif)
VALUES
    ('SANTE', 'Activites sante', true),
    ('EDUC', 'Activites education', true),
    ('NUTRI', 'Activites nutrition', true),
    ('ADMIN', 'Administration', true)
ON CONFLICT (code) DO NOTHING;

-- Lignes budget
INSERT INTO ligne_budget (financement_id, libelle, montant, exercice)
SELECT f.id, 'Fournitures sante', 5000000.00, 2026
FROM financement f WHERE f.code = 'UNICEF-2026'
AND NOT EXISTS (SELECT 1 FROM ligne_budget lb WHERE lb.financement_id = f.id AND lb.libelle = 'Fournitures sante');

INSERT INTO ligne_budget (financement_id, libelle, montant, exercice)
SELECT f.id, 'Vivres nutrition', 3000000.00, 2026
FROM financement f WHERE f.code = 'PAM-NUTRI'
AND NOT EXISTS (SELECT 1 FROM ligne_budget lb WHERE lb.financement_id = f.id AND lb.libelle = 'Vivres nutrition');

-- Tuteurs
INSERT INTO tuteur (nom, piece_identite_type, piece_identite_numero, telephone)
SELECT 'Amadou Diallo', 'CNI', 'ML-001234', '+223 70 12 34 56'
WHERE NOT EXISTS (SELECT 1 FROM tuteur WHERE nom = 'Amadou Diallo');

INSERT INTO tuteur (nom, piece_identite_type, piece_identite_numero, telephone)
SELECT 'Fatou Traore', 'Passeport', 'PS-98765', '+223 76 98 76 54'
WHERE NOT EXISTS (SELECT 1 FROM tuteur WHERE nom = 'Fatou Traore');

-- Enfants
INSERT INTO enfant (nom, date_naissance, sexe, tuteur_id, date_entree, statut, notes)
SELECT 'Ibrahim Diallo', '2015-03-10', 'M', t.id, '2025-09-01', 'ACTIF', 'Inscrit au programme sante'
FROM tuteur t WHERE t.nom = 'Amadou Diallo'
AND NOT EXISTS (SELECT 1 FROM enfant WHERE nom = 'Ibrahim Diallo');

INSERT INTO enfant (nom, date_naissance, sexe, tuteur_id, date_entree, statut, notes)
SELECT 'Mariam Traore', '2016-07-22', 'F', t.id, '2025-09-01', 'ACTIF', NULL
FROM tuteur t WHERE t.nom = 'Fatou Traore'
AND NOT EXISTS (SELECT 1 FROM enfant WHERE nom = 'Mariam Traore');

-- Aides
INSERT INTO aide (enfant_id, montant_mensuel, financement_id, reference_decision, date_debut, date_fin, statut)
SELECT e.id, 50000.00, f.id, 'DEC-2026-001', '2026-01-01', '2026-12-31', 'ACTIVE'
FROM enfant e, financement f
WHERE e.nom = 'Ibrahim Diallo' AND f.code = 'UNICEF-2026'
AND NOT EXISTS (SELECT 1 FROM aide a WHERE a.enfant_id = e.id AND a.financement_id = f.id);

INSERT INTO aide (enfant_id, montant_mensuel, financement_id, reference_decision, date_debut, date_fin, statut)
SELECT e.id, 35000.00, f.id, 'DEC-2026-002', '2026-01-01', NULL, 'ACTIVE'
FROM enfant e, financement f
WHERE e.nom = 'Mariam Traore' AND f.code = 'PAM-NUTRI'
AND NOT EXISTS (SELECT 1 FROM aide a WHERE a.enfant_id = e.id AND a.financement_id = f.id);
