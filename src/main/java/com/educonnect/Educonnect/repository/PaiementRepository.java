package com.educonnect.Educonnect.repository;

import com.educonnect.Educonnect.entity.Paiement;
import com.educonnect.Educonnect.entity.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    
    // Rechercher les paiements par élève
    List<Paiement> findByEleve(Eleve eleve);
    
    List<Paiement> findByEleveId(Long eleveId);
    
    // Rechercher les paiements par motif
    List<Paiement> findByMotifPaiementId(Long motifPaiementId);
    
    // Rechercher les paiements d'un élève pour un motif spécifique
    List<Paiement> findByEleveIdAndMotifPaiementId(Long eleveId, Long motifPaiementId);
    
    // Rechercher les paiements d'une classe
    @Query("SELECT p FROM Paiement p WHERE p.eleve.classe.id = :classeId")
    List<Paiement> findByClasseId(@Param("classeId") Long classeId);
    
    // Calculer le total versé par un élève (tous motifs confondus)
    @Query("SELECT SUM(p.montantVerse) FROM Paiement p WHERE p.eleve.id = :eleveId")
    BigDecimal getTotalPaiementsByEleveId(@Param("eleveId") Long eleveId);
    
    // Calculer le total versé par un élève pour un motif spécifique
    @Query("SELECT SUM(p.montantVerse) FROM Paiement p WHERE p.eleve.id = :eleveId AND p.motifPaiement.id = :motifPaiementId")
    BigDecimal getTotalPaiementsByEleveIdAndMotifId(@Param("eleveId") Long eleveId, @Param("motifPaiementId") Long motifPaiementId);
    
    // Obtenir l'historique des paiements d'un élève pour un motif (trié par date décroissante)
    @Query("SELECT p FROM Paiement p WHERE p.eleve.id = :eleveId AND p.motifPaiement.id = :motifPaiementId ORDER BY p.datePaiement DESC")
    List<Paiement> getHistoriquePaiements(@Param("eleveId") Long eleveId, @Param("motifPaiementId") Long motifPaiementId);
}
