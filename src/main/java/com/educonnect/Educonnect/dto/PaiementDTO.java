package com.educonnect.Educonnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDTO {
    
    private Long id;
    
    @NotNull(message = "L'ID de l'élève est obligatoire")
    private Long eleveId;
    
    private String eleveNom;
    private String elevePrenom;
    
    @NotNull(message = "L'ID du motif de paiement est obligatoire")
    private Long motifPaiementId;
    
    private String motifLibelle;
    private BigDecimal motifMontant;
    
    @NotNull(message = "Le montant versé est obligatoire")
    @Positive(message = "Le montant versé doit être positif")
    private BigDecimal montantVerse;
    
    private String datePaiement;
}
