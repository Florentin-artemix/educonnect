package com.educonnect.Educonnect.repository;

import com.educonnect.Educonnect.entity.MotifPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotifPaiementRepository extends JpaRepository<MotifPaiement, Long> {
    
    // Rechercher des motifs par libellé (contient)
    List<MotifPaiement> findByLibelleContainingIgnoreCase(String libelle);
    
    // Vérifier si un motif existe avec un libellé donné
    boolean existsByLibelleIgnoreCase(String libelle);
}
