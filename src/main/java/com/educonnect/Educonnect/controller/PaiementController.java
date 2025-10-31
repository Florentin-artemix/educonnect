package com.educonnect.Educonnect.controller;

import com.educonnect.Educonnect.dto.*;
import com.educonnect.Educonnect.entity.Eleve;
import com.educonnect.Educonnect.entity.MotifPaiement;
import com.educonnect.Educonnect.entity.Paiement;
import com.educonnect.Educonnect.repository.EleveRepository;
import com.educonnect.Educonnect.repository.MotifPaiementRepository;
import com.educonnect.Educonnect.repository.PaiementRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "*")
public class PaiementController {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private EleveRepository eleveRepository;

    @Autowired
    private MotifPaiementRepository motifPaiementRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // CREATE - Enregistrer un nouveau paiement
    @PostMapping
    public ResponseEntity<?> createPaiement(@Valid @RequestBody PaiementDTO paiementDTO) {
        try {
            Eleve eleve = eleveRepository.findById(paiementDTO.getEleveId())
                    .orElseThrow(() -> new RuntimeException("Élève non trouvé avec l'ID: " + paiementDTO.getEleveId()));

            MotifPaiement motifPaiement = motifPaiementRepository.findById(paiementDTO.getMotifPaiementId())
                    .orElseThrow(() -> new RuntimeException("Motif de paiement non trouvé avec l'ID: " + paiementDTO.getMotifPaiementId()));

            Paiement paiement = new Paiement();
            paiement.setEleve(eleve);
            paiement.setMotifPaiement(motifPaiement);
            paiement.setMontantVerse(paiementDTO.getMontantVerse());

            Paiement savedPaiement = paiementRepository.save(paiement);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedPaiement));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du paiement: " + e.getMessage());
        }
    }

    // READ - Récupérer tous les paiements
    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getAllPaiements() {
        List<PaiementDTO> paiements = paiementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(paiements);
    }

    // READ - Récupérer un paiement par ID
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getPaiementById(@PathVariable Long id) {
        return paiementRepository.findById(id)
                .map(paiement -> ResponseEntity.ok(convertToDTO(paiement)))
                .orElse(ResponseEntity.notFound().build());
    }

    // READ - Récupérer les paiements d'un élève
    @GetMapping("/eleve/{eleveId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByEleve(@PathVariable Long eleveId) {
        List<PaiementDTO> paiements = paiementRepository.findByEleveId(eleveId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(paiements);
    }

    // READ - Récupérer les paiements pour un motif spécifique
    @GetMapping("/motif/{motifId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByMotif(@PathVariable Long motifId) {
        List<PaiementDTO> paiements = paiementRepository.findByMotifPaiementId(motifId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(paiements);
    }

    // SUIVI - Récupérer le suivi des paiements d'un élève pour un motif
    @GetMapping("/suivi/eleve/{eleveId}/motif/{motifId}")
    public ResponseEntity<?> getSuiviPaiement(@PathVariable Long eleveId, @PathVariable Long motifId) {
        try {
            Eleve eleve = eleveRepository.findById(eleveId)
                    .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

            MotifPaiement motifPaiement = motifPaiementRepository.findById(motifId)
                    .orElseThrow(() -> new RuntimeException("Motif de paiement non trouvé"));

            // Récupérer l'historique des paiements
            List<Paiement> paiements = paiementRepository.getHistoriquePaiements(eleveId, motifId);

            // Calculer le total versé
            BigDecimal totalVerse = paiementRepository.getTotalPaiementsByEleveIdAndMotifId(eleveId, motifId);
            if (totalVerse == null) {
                totalVerse = BigDecimal.ZERO;
            }

            // Calculer le reste à payer
            BigDecimal resteAPayer = motifPaiement.getMontant().subtract(totalVerse);

            // Calculer le pourcentage payé
            Double pourcentagePaye = motifPaiement.getMontant().compareTo(BigDecimal.ZERO) > 0
                    ? totalVerse.divide(motifPaiement.getMontant(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue()
                    : 0.0;

            // Déterminer le statut
            String statut = resteAPayer.compareTo(BigDecimal.ZERO) <= 0 ? "SOLDÉ" : "PAIEMENT_EN_COURS";

            // Date du dernier versement
            String dateDernierVersement = paiements.isEmpty() ? null
                    : paiements.get(0).getDatePaiement().format(formatter);

            // Créer l'historique des paiements
            List<PaiementHistoriqueDTO> historique = paiements.stream()
                    .map(p -> new PaiementHistoriqueDTO(
                            p.getId(),
                            p.getMontantVerse(),
                            p.getDatePaiement().format(formatter)
                    ))
                    .collect(Collectors.toList());

            // Créer le DTO de suivi
            SuiviPaiementDTO suiviDTO = new SuiviPaiementDTO();
            suiviDTO.setEleveId(eleve.getId());
            suiviDTO.setEleveNom(eleve.getNom());
            suiviDTO.setElevePrenom(eleve.getPrenom());
            suiviDTO.setMotifPaiementId(motifPaiement.getId());
            suiviDTO.setMotifLibelle(motifPaiement.getLibelle());
            suiviDTO.setMotifMontant(motifPaiement.getMontant());
            suiviDTO.setHistoriquePaiements(historique);
            suiviDTO.setTotalVerse(totalVerse);
            suiviDTO.setResteAPayer(resteAPayer.max(BigDecimal.ZERO));
            suiviDTO.setPourcentagePaye(pourcentagePaye);
            suiviDTO.setStatut(statut);
            suiviDTO.setDateDernierVersement(dateDernierVersement);

            return ResponseEntity.ok(suiviDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du suivi: " + e.getMessage());
        }
    }

    // SUIVI - Récupérer tous les suivis de paiement pour un élève
    @GetMapping("/suivi/eleve/{eleveId}")
    public ResponseEntity<?> getAllSuivisByEleve(@PathVariable Long eleveId) {
        try {
            Eleve eleve = eleveRepository.findById(eleveId)
                    .orElseThrow(() -> new RuntimeException("Élève non trouvé"));

            List<MotifPaiement> motifs = motifPaiementRepository.findAll();
            
            List<SuiviPaiementDTO> suivis = motifs.stream()
                    .map(motif -> {
                        try {
                            ResponseEntity<?> response = getSuiviPaiement(eleveId, motif.getId());
                            return (SuiviPaiementDTO) response.getBody();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(suivi -> suivi != null && !suivi.getHistoriquePaiements().isEmpty())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(suivis);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE - Supprimer un paiement
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaiement(@PathVariable Long id) {
        if (!paiementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        paiementRepository.deleteById(id);
        return ResponseEntity.ok().body("Paiement supprimé avec succès");
    }

    // Méthode utilitaire pour convertir Entity -> DTO
    private PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setEleveId(paiement.getEleve() != null ? paiement.getEleve().getId() : null);
        dto.setEleveNom(paiement.getEleve() != null ? paiement.getEleve().getNom() : null);
        dto.setElevePrenom(paiement.getEleve() != null ? paiement.getEleve().getPrenom() : null);
        dto.setMotifPaiementId(paiement.getMotifPaiement() != null ? paiement.getMotifPaiement().getId() : null);
        dto.setMotifLibelle(paiement.getMotifPaiement() != null ? paiement.getMotifPaiement().getLibelle() : null);
        dto.setMotifMontant(paiement.getMotifPaiement() != null ? paiement.getMotifPaiement().getMontant() : null);
        dto.setMontantVerse(paiement.getMontantVerse());
        dto.setDatePaiement(paiement.getDatePaiement() != null ? paiement.getDatePaiement().format(formatter) : null);
        return dto;
    }
}
