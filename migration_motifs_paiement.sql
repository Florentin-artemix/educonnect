-- ========================================
-- SCRIPT DE MIGRATION - MOTIFS DE PAIEMENT
-- Base de données : postgres
-- À exécuter dans pgAdmin ou DBeaver
-- ========================================

-- ========================================
-- ÉTAPE 1 : CORRIGER LES CONTRAINTES EXISTANTES
-- ========================================

-- 1.1 CONTRAINTE COMMUNICATIONS - TYPE
ALTER TABLE communications DROP CONSTRAINT IF EXISTS communications_type_check;
ALTER TABLE communications ADD CONSTRAINT communications_type_check 
CHECK (type IN ('INFORMATION', 'ALERTE', 'RAPPEL', 'CONVOCATION'));

-- 1.2 CONTRAINTE ELEVES - STATUT_PAIEMENT
ALTER TABLE eleves DROP CONSTRAINT IF EXISTS eleves_statut_paiement_check;
ALTER TABLE eleves ADD CONSTRAINT eleves_statut_paiement_check 
CHECK (statut_paiement IN ('DEROGATION', 'NON_EN_ORDRE', 'EN_ORDRE'));

-- ========================================
-- ÉTAPE 2 : CRÉER LA TABLE MOTIFS_PAIEMENT
-- ========================================

CREATE TABLE IF NOT EXISTS motifs_paiement (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    montant NUMERIC(10, 2) NOT NULL,
    date_creation TIMESTAMP,
    date_modification TIMESTAMP,
    CONSTRAINT motifs_paiement_libelle_unique UNIQUE (libelle),
    CONSTRAINT motifs_paiement_montant_check CHECK (montant > 0)
);

-- ========================================
-- ÉTAPE 3 : SAUVEGARDER LES ANCIENNES DONNÉES PAIEMENTS
-- ========================================

-- Créer une table temporaire pour sauvegarder les anciennes données
CREATE TABLE IF NOT EXISTS paiements_backup AS 
SELECT * FROM paiements;

-- ========================================
-- ÉTAPE 4 : MODIFIER LA TABLE PAIEMENTS
-- ========================================

-- Supprimer l'ancienne contrainte trimestre
ALTER TABLE paiements DROP CONSTRAINT IF EXISTS paiements_trimestre_check;

-- Ajouter la nouvelle colonne motif_paiement_id (nullable temporairement)
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS motif_paiement_id BIGINT;

-- Ajouter la nouvelle colonne montant_verse (nullable temporairement)
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS montant_verse NUMERIC(10, 2);

-- Ajouter la nouvelle colonne date_paiement
ALTER TABLE paiements ADD COLUMN IF NOT EXISTS date_paiement TIMESTAMP;

-- ========================================
-- ÉTAPE 4.1 : MIGRER LES DONNÉES EXISTANTES
-- ========================================

-- Mettre à jour les enregistrements existants avec les nouvelles colonnes
-- En fonction du trimestre, assigner le motif correspondant
UPDATE paiements SET motif_paiement_id = 1, montant_verse = montant_paye, date_paiement = date_maj 
WHERE trimestre = 'TRIMESTRE_1' AND motif_paiement_id IS NULL;

UPDATE paiements SET motif_paiement_id = 2, montant_verse = montant_paye, date_paiement = date_maj 
WHERE trimestre = 'TRIMESTRE_2' AND motif_paiement_id IS NULL;

UPDATE paiements SET motif_paiement_id = 3, montant_verse = montant_paye, date_paiement = date_maj 
WHERE trimestre = 'TRIMESTRE_3' AND motif_paiement_id IS NULL;

-- Si des enregistrements n'ont pas de trimestre, leur assigner le motif 1 par défaut
UPDATE paiements SET motif_paiement_id = 1, montant_verse = COALESCE(montant_paye, 0), date_paiement = COALESCE(date_maj, NOW())
WHERE motif_paiement_id IS NULL;

-- ========================================
-- ÉTAPE 4.2 : SUPPRIMER LES ANCIENNES COLONNES
-- ========================================

-- Maintenant qu'on a migré les données, on peut supprimer les anciennes colonnes
ALTER TABLE paiements DROP COLUMN IF EXISTS trimestre;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_total;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_paye;
ALTER TABLE paiements DROP COLUMN IF EXISTS date_maj;

-- ========================================
-- ÉTAPE 4.3 : AJOUTER LES CONTRAINTES
-- ========================================

-- Ajouter la contrainte de clé étrangère
ALTER TABLE paiements 
DROP CONSTRAINT IF EXISTS fk_paiements_motif_paiement;

ALTER TABLE paiements 
ADD CONSTRAINT fk_paiements_motif_paiement 
FOREIGN KEY (motif_paiement_id) 
REFERENCES motifs_paiement(id) 
ON DELETE RESTRICT;

-- Ajouter les contraintes NOT NULL
ALTER TABLE paiements ALTER COLUMN eleve_id SET NOT NULL;
ALTER TABLE paiements ALTER COLUMN motif_paiement_id SET NOT NULL;
ALTER TABLE paiements ALTER COLUMN montant_verse SET NOT NULL;

-- Ajouter la contrainte CHECK pour montant_verse
ALTER TABLE paiements DROP CONSTRAINT IF EXISTS paiements_montant_verse_check;
ALTER TABLE paiements ADD CONSTRAINT paiements_montant_verse_check CHECK (montant_verse > 0);

-- ========================================
-- ÉTAPE 5 : CRÉER DES MOTIFS PAR DÉFAUT
-- ========================================

INSERT INTO motifs_paiement (libelle, montant, date_creation, date_modification)
VALUES 
    ('Frais scolaire Premier Trimestre', 150000.00, NOW(), NOW()),
    ('Frais scolaire Deuxième Trimestre', 150000.00, NOW(), NOW()),
    ('Frais scolaire Troisième Trimestre', 150000.00, NOW(), NOW())
ON CONFLICT (libelle) DO NOTHING;

-- ========================================
-- ÉTAPE 6 : MIGRER LES ANCIENNES DONNÉES (SI NÉCESSAIRE)
-- ========================================

-- Si vous avez des données dans paiements_backup, vous pouvez les migrer manuellement
-- en créant de nouveaux paiements avec les motifs appropriés
-- Exemple :
-- INSERT INTO paiements (eleve_id, motif_paiement_id, montant_verse, date_paiement)
-- SELECT 
--     pb.eleve_id, 
--     1, -- ID du motif correspondant
--     pb.montant_paye, 
--     pb.date_maj
-- FROM paiements_backup pb
-- WHERE pb.trimestre = 'TRIMESTRE_1';

-- ========================================
-- ÉTAPE 7 : VÉRIFICATION
-- ========================================

-- Vérifier la structure de la table motifs_paiement
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'motifs_paiement'
ORDER BY ordinal_position;

-- Vérifier la structure de la table paiements
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- Vérifier les contraintes
SELECT conname, conrelid::regclass AS table_name, pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint 
WHERE conrelid::regclass::text IN ('motifs_paiement', 'paiements')
ORDER BY conrelid::regclass::text, conname;

-- Vérifier les motifs créés
SELECT * FROM motifs_paiement;

-- ========================================
-- ÉTAPE 8 : NETTOYAGE (OPTIONNEL)
-- ========================================

-- Une fois que vous êtes sûr que tout fonctionne, vous pouvez supprimer la backup
-- DROP TABLE IF EXISTS paiements_backup;

-- ========================================
-- FIN DU SCRIPT
-- ========================================
