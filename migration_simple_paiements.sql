-- ========================================
-- SCRIPT SIMPLIFIÉ DE MIGRATION PAIEMENTS
-- Exécutez ce script étape par étape
-- ========================================

-- ========================================
-- ÉTAPE 1 : VÉRIFICATION DE LA STRUCTURE
-- ========================================

-- Vérifier les colonnes existantes dans paiements
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- Compter le nombre de paiements existants
SELECT COUNT(*) as nb_paiements FROM paiements;

-- ========================================
-- ÉTAPE 2 : CRÉER LES MOTIFS DE PAIEMENT PAR DÉFAUT
-- ========================================

-- Créer 3 motifs par défaut (si ils n'existent pas)
INSERT INTO motifs_paiement (id, libelle, montant, date_creation, date_modification)
VALUES 
    (1, 'Frais scolaire Premier Trimestre', 150000.00, NOW(), NOW()),
    (2, 'Frais scolaire Deuxième Trimestre', 150000.00, NOW(), NOW()),
    (3, 'Frais scolaire Troisième Trimestre', 150000.00, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Réinitialiser la séquence pour les prochains IDs
SELECT setval('motifs_paiement_id_seq', (SELECT MAX(id) FROM motifs_paiement));

-- ========================================
-- ÉTAPE 3 : AJOUTER LES NOUVELLES COLONNES SI NÉCESSAIRE
-- ========================================

-- Ajouter motif_paiement_id (nullable temporairement)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'paiements' AND column_name = 'motif_paiement_id') THEN
        ALTER TABLE paiements ADD COLUMN motif_paiement_id BIGINT;
    END IF;
END $$;

-- Ajouter montant_verse (nullable temporairement)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'paiements' AND column_name = 'montant_verse') THEN
        ALTER TABLE paiements ADD COLUMN montant_verse NUMERIC(10, 2);
    END IF;
END $$;

-- Ajouter date_paiement
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'paiements' AND column_name = 'date_paiement') THEN
        ALTER TABLE paiements ADD COLUMN date_paiement TIMESTAMP;
    END IF;
END $$;

-- ========================================
-- ÉTAPE 4 : MIGRER LES DONNÉES EXISTANTES
-- ========================================

-- Si la colonne trimestre existe, migrer les données
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'paiements' AND column_name = 'trimestre') THEN
        
        -- Migrer TRIMESTRE_1
        UPDATE paiements 
        SET motif_paiement_id = 1, 
            montant_verse = COALESCE(montant_paye, 0), 
            date_paiement = COALESCE(date_maj, NOW())
        WHERE trimestre = 'TRIMESTRE_1' AND motif_paiement_id IS NULL;
        
        -- Migrer TRIMESTRE_2
        UPDATE paiements 
        SET motif_paiement_id = 2, 
            montant_verse = COALESCE(montant_paye, 0), 
            date_paiement = COALESCE(date_maj, NOW())
        WHERE trimestre = 'TRIMESTRE_2' AND motif_paiement_id IS NULL;
        
        -- Migrer TRIMESTRE_3
        UPDATE paiements 
        SET motif_paiement_id = 3, 
            montant_verse = COALESCE(montant_paye, 0), 
            date_paiement = COALESCE(date_maj, NOW())
        WHERE trimestre = 'TRIMESTRE_3' AND motif_paiement_id IS NULL;
        
    END IF;
END $$;

-- Pour les enregistrements restants sans motif, assigner le motif 1 par défaut
UPDATE paiements 
SET motif_paiement_id = COALESCE(motif_paiement_id, 1),
    montant_verse = COALESCE(montant_verse, montant_paye, montant_total, 0),
    date_paiement = COALESCE(date_paiement, date_maj, NOW())
WHERE motif_paiement_id IS NULL;

-- ========================================
-- ÉTAPE 5 : SUPPRIMER LES ANCIENNES COLONNES
-- ========================================

ALTER TABLE paiements DROP COLUMN IF EXISTS trimestre;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_total;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_paye;
ALTER TABLE paiements DROP COLUMN IF EXISTS date_maj;

-- ========================================
-- ÉTAPE 6 : AJOUTER LES CONTRAINTES
-- ========================================

-- Supprimer les anciennes contraintes si elles existent
ALTER TABLE paiements DROP CONSTRAINT IF EXISTS fk_paiements_motif_paiement;
ALTER TABLE paiements DROP CONSTRAINT IF EXISTS paiements_montant_verse_check;
ALTER TABLE paiements DROP CONSTRAINT IF EXISTS paiements_trimestre_check;

-- Ajouter la contrainte de clé étrangère
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
ALTER TABLE paiements ADD CONSTRAINT paiements_montant_verse_check CHECK (montant_verse > 0);

-- ========================================
-- ÉTAPE 7 : VÉRIFICATION FINALE
-- ========================================

-- Vérifier la structure finale
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- Vérifier qu'il n'y a pas de NULL dans motif_paiement_id
SELECT COUNT(*) as nb_sans_motif FROM paiements WHERE motif_paiement_id IS NULL;

-- Voir quelques exemples de données migrées
SELECT * FROM paiements LIMIT 5;

-- Vérifier les contraintes
SELECT conname, conrelid::regclass AS table_name, pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint 
WHERE conrelid::regclass::text = 'paiements'
ORDER BY conname;

-- ========================================
-- FIN DU SCRIPT - MIGRATION TERMINÉE ✅
-- ========================================
