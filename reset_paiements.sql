-- ========================================
-- SCRIPT DE NETTOYAGE ET MIGRATION COMPLÈTE
-- Exécutez ce script pour tout remettre à zéro
-- ========================================

-- ÉTAPE 1 : Supprimer complètement la table paiements et la recréer
DROP TABLE IF EXISTS paiements CASCADE;

-- ÉTAPE 2 : Recréer la table paiements avec la nouvelle structure
CREATE TABLE paiements (
    id BIGSERIAL PRIMARY KEY,
    eleve_id BIGINT NOT NULL,
    motif_paiement_id BIGINT NOT NULL,
    montant_verse NUMERIC(10, 2) NOT NULL CHECK (montant_verse > 0),
    date_paiement TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_paiements_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiements_motif_paiement FOREIGN KEY (motif_paiement_id) REFERENCES motifs_paiement(id) ON DELETE RESTRICT
);

-- ÉTAPE 3 : Vérifier la structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'paiements'
ORDER BY ordinal_position;

-- ÉTAPE 4 : Vérifier les motifs créés
SELECT * FROM motifs_paiement;

-- ========================================
-- FIN - TABLE PAIEMENTS RECRÉÉE ✅
-- ========================================
