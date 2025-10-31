package com.educonnect.Educonnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;
    
    @ManyToOne
    @JoinColumn(name = "motif_paiement_id", nullable = false)
    private MotifPaiement motifPaiement;
    
    @Column(name = "montant_verse", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantVerse;
    
    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;
    
    @PrePersist
    protected void onCreate() {
        datePaiement = LocalDateTime.now();
    }
}
