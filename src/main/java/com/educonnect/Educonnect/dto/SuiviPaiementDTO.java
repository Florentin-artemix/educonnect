package com.educonnect.Educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuiviPaiementDTO {
    
    private Long eleveId;
    private String eleveNom;
    private String elevePrenom;
    
    private Long motifPaiementId;
    private String motifLibelle;
    private BigDecimal motifMontant;
    
    private List<PaiementHistoriqueDTO> historiquePaiements;
    private BigDecimal totalVerse;
    private BigDecimal resteAPayer;
    private Double pourcentagePaye;
    private String statut; // "SOLDÉ" ou "PAIEMENT_EN_COURS"
    private String dateDernierVersement;
}
