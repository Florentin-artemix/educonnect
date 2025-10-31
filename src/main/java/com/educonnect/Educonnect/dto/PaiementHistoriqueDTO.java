package com.educonnect.Educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementHistoriqueDTO {
    
    private Long paiementId;
    private BigDecimal montantVerse;
    private String datePaiement;
}
