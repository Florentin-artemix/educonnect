package com.educonnect.Educonnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotifPaiementDTO {
    
    private Long id;
    
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;
    
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;
    
    private String dateCreation;
    private String dateModification;
}
