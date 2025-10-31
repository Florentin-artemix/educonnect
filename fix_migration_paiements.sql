-- ========================================
-- SCRIPT DE CORRECTION RAPIDE
-- Pour corriger l'erreur de migration
-- ========================================

-- ÉTAPE 1 : Vérifier la structure actuelle de la table paiements
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- ÉTAPE 2 : Vérifier s'il y a des données dans paiements
SELECT COUNT(*) as nb_paiements FROM paiements;

-- ========================================
-- OPTION A : SI VOUS AVEZ DES DONNÉES À CONSERVER
-- ========================================

-- Mettre à jour les enregistrements avec trimestre
UPDATE paiements 
SET motif_paiement_id = 1, 
    montant_verse = COALESCE(montant_paye, 0), 
    date_paiement = COALESCE(date_maj, NOW())
WHERE trimestre = 'TRIMESTRE_1' AND motif_paiement_id IS NULL;

UPDATE paiements 
SET motif_paiement_id = 2, 
    montant_verse = COALESCE(montant_paye, 0), 
    date_paiement = COALESCE(date_maj, NOW())
WHERE trimestre = 'TRIMESTRE_2' AND motif_paiement_id IS NULL;

UPDATE paiements 
SET motif_paiement_id = 3, 
    montant_verse = COALESCE(montant_paye, 0), 
    date_paiement = COALESCE(date_maj, NOW())
WHERE trimestre = 'TRIMESTRE_3' AND motif_paiement_id IS NULL;

-- Pour les enregistrements sans trimestre, assigner le motif 1 par défaut
UPDATE paiements 
SET motif_paiement_id = 1, 
    montant_verse = COALESCE(montant_paye, montant_total, 0), 
    date_paiement = COALESCE(date_maj, NOW())
WHERE motif_paiement_id IS NULL;

-- ========================================
-- OPTION B : SI VOUS VOULEZ REPARTIR DE ZÉRO (DONNÉES DE TEST)
-- ========================================

-- Décommenter cette ligne pour vider la table paiements
-- TRUNCATE TABLE paiements CASCADE;

-- ========================================
-- ÉTAPE 3 : SUPPRIMER LES ANCIENNES COLONNES
-- ========================================

ALTER TABLE paiements DROP COLUMN IF EXISTS trimestre;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_total;
ALTER TABLE paiements DROP COLUMN IF EXISTS montant_paye;
ALTER TABLE paiements DROP COLUMN IF EXISTS date_maj;

-- ========================================
-- ÉTAPE 4 : AJOUTER LES CONTRAINTES
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
-- ÉTAPE 5 : VÉRIFICATION FINALE
-- ========================================

-- Vérifier que tous les paiements ont un motif
SELECT COUNT(*) as nb_sans_motif FROM paiements WHERE motif_paiement_id IS NULL;

-- Vérifier la structure finale
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- Vérifier les données migrées
SELECT id, eleve_id, motif_paiement_id, montant_verse, date_paiement
FROM paiements LIMIT 5;

-- ========================================
-- FIN DU SCRIPT
-- ========================================
