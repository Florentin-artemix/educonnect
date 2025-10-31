package com.educonnect.Educonnect.controller;

import com.educonnect.Educonnect.dto.MotifPaiementDTO;
import com.educonnect.Educonnect.entity.MotifPaiement;
import com.educonnect.Educonnect.repository.MotifPaiementRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/motifs-paiement")
@CrossOrigin(origins = "*")
public class MotifPaiementController {
    
    @Autowired
    private MotifPaiementRepository motifPaiementRepository;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // CREATE - Créer un nouveau motif de paiement
    @PostMapping
    public ResponseEntity<?> createMotifPaiement(@Valid @RequestBody MotifPaiementDTO motifDTO) {
        try {
            // Vérifier si un motif avec le même libellé existe déjà
            if (motifPaiementRepository.existsByLibelleIgnoreCase(motifDTO.getLibelle())) {
                return ResponseEntity.badRequest().body("Un motif avec ce libellé existe déjà");
            }
            
            MotifPaiement motif = new MotifPaiement();
            motif.setLibelle(motifDTO.getLibelle());
            motif.setMontant(motifDTO.getMontant());
            
            MotifPaiement savedMotif = motifPaiementRepository.save(motif);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedMotif));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du motif : " + e.getMessage());
        }
    }
    
    // READ - Récupérer tous les motifs de paiement
    @GetMapping
    public ResponseEntity<List<MotifPaiementDTO>> getAllMotifsPaiement() {
        List<MotifPaiement> motifs = motifPaiementRepository.findAll();
        List<MotifPaiementDTO> motifsDTO = motifs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(motifsDTO);
    }
    
    // READ - Récupérer un motif par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMotifPaiementById(@PathVariable Long id) {
        return motifPaiementRepository.findById(id)
                .map(motif -> ResponseEntity.ok(convertToDTO(motif)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // READ - Rechercher des motifs par libellé
    @GetMapping("/search")
    public ResponseEntity<List<MotifPaiementDTO>> searchMotifsByLibelle(@RequestParam String libelle) {
        List<MotifPaiement> motifs = motifPaiementRepository.findByLibelleContainingIgnoreCase(libelle);
        List<MotifPaiementDTO> motifsDTO = motifs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(motifsDTO);
    }
    
    // UPDATE - Mettre à jour un motif de paiement
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMotifPaiement(@PathVariable Long id, 
                                                  @Valid @RequestBody MotifPaiementDTO motifDTO) {
        try {
            return motifPaiementRepository.findById(id)
                    .map(existingMotif -> {
                        // Vérifier si le nouveau libellé existe déjà (sauf pour le motif actuel)
                        if (!existingMotif.getLibelle().equalsIgnoreCase(motifDTO.getLibelle()) &&
                            motifPaiementRepository.existsByLibelleIgnoreCase(motifDTO.getLibelle())) {
                            return ResponseEntity.badRequest().body("Un motif avec ce libellé existe déjà");
                        }
                        
                        existingMotif.setLibelle(motifDTO.getLibelle());
                        existingMotif.setMontant(motifDTO.getMontant());
                        
                        MotifPaiement updatedMotif = motifPaiementRepository.save(existingMotif);
                        return ResponseEntity.ok(convertToDTO(updatedMotif));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du motif : " + e.getMessage());
        }
    }
    
    // DELETE - Supprimer un motif de paiement
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMotifPaiement(@PathVariable Long id) {
        try {
            return motifPaiementRepository.findById(id)
                    .map(motif -> {
                        // Vérifier si des paiements sont liés à ce motif
                        if (motif.getPaiements() != null && !motif.getPaiements().isEmpty()) {
                            return ResponseEntity.badRequest()
                                    .body("Impossible de supprimer ce motif : des paiements y sont associés");
                        }
                        
                        motifPaiementRepository.delete(motif);
                        return ResponseEntity.ok().body("Motif de paiement supprimé avec succès");
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du motif : " + e.getMessage());
        }
    }
    
    // Méthode utilitaire pour convertir Entity -> DTO
    private MotifPaiementDTO convertToDTO(MotifPaiement motif) {
        MotifPaiementDTO dto = new MotifPaiementDTO();
        dto.setId(motif.getId());
        dto.setLibelle(motif.getLibelle());
        dto.setMontant(motif.getMontant());
        dto.setDateCreation(motif.getDateCreation() != null ? motif.getDateCreation().format(formatter) : null);
        dto.setDateModification(motif.getDateModification() != null ? motif.getDateModification().format(formatter) : null);
        return dto;
    }
}
