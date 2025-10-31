# 📚 DOCUMENTATION API - GESTION DES MOTIFS DE PAIEMENT

## 🎯 Vue d'ensemble

Cette documentation décrit les API REST pour gérer les motifs de paiement et le suivi des paiements des élèves dans le système EduConnect.

---

## 📋 API MOTIFS DE PAIEMENT

**Base URL:** `/api/motifs-paiement`

### 1. Créer un motif de paiement

**POST** `/api/motifs-paiement`

**Body (JSON):**
```json
{
  "libelle": "Frais scolaire Premier Trimestre",
  "montant": 150000.00
}
```

**Réponse (201 Created):**
```json
{
  "id": 1,
  "libelle": "Frais scolaire Premier Trimestre",
  "montant": 150000.00,
  "dateCreation": "2025-10-31 10:00:00",
  "dateModification": "2025-10-31 10:00:00"
}
```

---

### 2. Récupérer tous les motifs

**GET** `/api/motifs-paiement`

**Réponse (200 OK):**
```json
[
  {
    "id": 1,
    "libelle": "Frais scolaire Premier Trimestre",
    "montant": 150000.00,
    "dateCreation": "2025-10-31 10:00:00",
    "dateModification": "2025-10-31 10:00:00"
  },
  {
    "id": 2,
    "libelle": "Frais scolaire Deuxième Trimestre",
    "montant": 150000.00,
    "dateCreation": "2025-10-31 10:01:00",
    "dateModification": "2025-10-31 10:01:00"
  }
]
```

---

### 3. Récupérer un motif par ID

**GET** `/api/motifs-paiement/{id}`

**Exemple:** `/api/motifs-paiement/1`

**Réponse (200 OK):**
```json
{
  "id": 1,
  "libelle": "Frais scolaire Premier Trimestre",
  "montant": 150000.00,
  "dateCreation": "2025-10-31 10:00:00",
  "dateModification": "2025-10-31 10:00:00"
}
```

---

### 4. Rechercher des motifs par libellé

**GET** `/api/motifs-paiement/search?libelle={texte}`

**Exemple:** `/api/motifs-paiement/search?libelle=Premier`

**Réponse (200 OK):**
```json
[
  {
    "id": 1,
    "libelle": "Frais scolaire Premier Trimestre",
    "montant": 150000.00,
    "dateCreation": "2025-10-31 10:00:00",
    "dateModification": "2025-10-31 10:00:00"
  }
]
```

---

### 5. Mettre à jour un motif

**PUT** `/api/motifs-paiement/{id}`

**Body (JSON):**
```json
{
  "libelle": "Frais scolaire Premier Trimestre - Modifié",
  "montant": 175000.00
}
```

**Réponse (200 OK):**
```json
{
  "id": 1,
  "libelle": "Frais scolaire Premier Trimestre - Modifié",
  "montant": 175000.00,
  "dateCreation": "2025-10-31 10:00:00",
  "dateModification": "2025-10-31 11:30:00"
}
```

---

### 6. Supprimer un motif

**DELETE** `/api/motifs-paiement/{id}`

**Réponse (200 OK):**
```
"Motif de paiement supprimé avec succès"
```

**Note:** La suppression est refusée si des paiements sont associés au motif.

---

## 💰 API PAIEMENTS

**Base URL:** `/api/paiements`

### 1. Enregistrer un paiement

**POST** `/api/paiements`

**Body (JSON):**
```json
{
  "eleveId": 1,
  "motifPaiementId": 1,
  "montantVerse": 50000.00
}
```

**Réponse (201 Created):**
```json
{
  "id": 1,
  "eleveId": 1,
  "eleveNom": "Dupont",
  "elevePrenom": "Jean",
  "motifPaiementId": 1,
  "motifLibelle": "Frais scolaire Premier Trimestre",
  "motifMontant": 150000.00,
  "montantVerse": 50000.00,
  "datePaiement": "2025-10-31 14:30:00"
}
```

---

### 2. Récupérer tous les paiements

**GET** `/api/paiements`

**Réponse (200 OK):**
```json
[
  {
    "id": 1,
    "eleveId": 1,
    "eleveNom": "Dupont",
    "elevePrenom": "Jean",
    "motifPaiementId": 1,
    "motifLibelle": "Frais scolaire Premier Trimestre",
    "motifMontant": 150000.00,
    "montantVerse": 50000.00,
    "datePaiement": "2025-10-31 14:30:00"
  }
]
```

---

### 3. Récupérer un paiement par ID

**GET** `/api/paiements/{id}`

---

### 4. Récupérer les paiements d'un élève

**GET** `/api/paiements/eleve/{eleveId}`

**Exemple:** `/api/paiements/eleve/1`

---

### 5. Récupérer les paiements pour un motif

**GET** `/api/paiements/motif/{motifId}`

**Exemple:** `/api/paiements/motif/1`

---

### 6. Supprimer un paiement

**DELETE** `/api/paiements/{id}`

**Réponse (200 OK):**
```
"Paiement supprimé avec succès"
```

---

## 📊 API SUIVI DES PAIEMENTS

### 1. Suivi d'un élève pour un motif spécifique

**GET** `/api/paiements/suivi/eleve/{eleveId}/motif/{motifId}`

**Exemple:** `/api/paiements/suivi/eleve/1/motif/1`

**Réponse (200 OK):**
```json
{
  "eleveId": 1,
  "eleveNom": "Dupont",
  "elevePrenom": "Jean",
  "motifPaiementId": 1,
  "motifLibelle": "Frais scolaire Premier Trimestre",
  "motifMontant": 150000.00,
  "historiquePaiements": [
    {
      "paiementId": 3,
      "montantVerse": 20000.00,
      "datePaiement": "2025-10-15 09:15:00"
    },
    {
      "paiementId": 2,
      "montantVerse": 30000.00,
      "datePaiement": "2025-09-30 14:45:00"
    },
    {
      "paiementId": 1,
      "montantVerse": 50000.00,
      "datePaiement": "2025-09-15 10:30:00"
    }
  ],
  "totalVerse": 100000.00,
  "resteAPayer": 50000.00,
  "pourcentagePaye": 66.67,
  "statut": "PAIEMENT_EN_COURS",
  "dateDernierVersement": "2025-10-15 09:15:00"
}
```

**Statuts possibles:**
- `SOLDÉ` : Le paiement est complet
- `PAIEMENT_EN_COURS` : Paiement partiel

---

### 2. Tous les suivis de paiement d'un élève

**GET** `/api/paiements/suivi/eleve/{eleveId}`

**Exemple:** `/api/paiements/suivi/eleve/1`

**Réponse (200 OK):**
```json
[
  {
    "eleveId": 1,
    "eleveNom": "Dupont",
    "elevePrenom": "Jean",
    "motifPaiementId": 1,
    "motifLibelle": "Frais scolaire Premier Trimestre",
    "motifMontant": 150000.00,
    "totalVerse": 150000.00,
    "resteAPayer": 0.00,
    "pourcentagePaye": 100.0,
    "statut": "SOLDÉ",
    "dateDernierVersement": "2025-10-01 16:20:00",
    "historiquePaiements": [...]
  },
  {
    "eleveId": 1,
    "eleveNom": "Dupont",
    "elevePrenom": "Jean",
    "motifPaiementId": 2,
    "motifLibelle": "Frais scolaire Deuxième Trimestre",
    "motifMontant": 150000.00,
    "totalVerse": 75000.00,
    "resteAPayer": 75000.00,
    "pourcentagePaye": 50.0,
    "statut": "PAIEMENT_EN_COURS",
    "dateDernierVersement": "2025-10-20 11:00:00",
    "historiquePaiements": [...]
  }
]
```

---

## 🔒 VALIDATIONS

### Motifs de Paiement
- **Libellé:** Obligatoire, unique (insensible à la casse)
- **Montant:** Obligatoire, doit être > 0

### Paiements
- **eleveId:** Obligatoire, doit exister dans la table eleves
- **motifPaiementId:** Obligatoire, doit exister dans la table motifs_paiement
- **montantVerse:** Obligatoire, doit être > 0

---

## ⚠️ CODES D'ERREUR

- **200 OK:** Succès
- **201 Created:** Ressource créée avec succès
- **400 Bad Request:** Données invalides ou contraintes violées
- **404 Not Found:** Ressource non trouvée
- **500 Internal Server Error:** Erreur serveur

---

## 📝 EXEMPLES D'UTILISATION

### Scénario complet : Gérer les paiements d'un élève

#### 1. Créer un motif
```bash
POST /api/motifs-paiement
{
  "libelle": "Frais scolaire Premier Trimestre",
  "montant": 150000.00
}
```

#### 2. Enregistrer un premier paiement
```bash
POST /api/paiements
{
  "eleveId": 1,
  "motifPaiementId": 1,
  "montantVerse": 50000.00
}
```

#### 3. Enregistrer un deuxième paiement
```bash
POST /api/paiements
{
  "eleveId": 1,
  "motifPaiementId": 1,
  "montantVerse": 50000.00
}
```

#### 4. Consulter le suivi
```bash
GET /api/paiements/suivi/eleve/1/motif/1
```

**Résultat:**
- Total versé: 100 000 FCFA
- Reste à payer: 50 000 FCFA
- Progression: 66.67%
- Statut: PAIEMENT_EN_COURS

---

## 🎓 FONCTIONNALITÉS CLÉS

✅ **CRUD complet** sur les motifs de paiement  
✅ **Enregistrement de paiements** avec horodatage automatique  
✅ **Historique des paiements** trié par date décroissante  
✅ **Calcul automatique** du total versé et du reste à payer  
✅ **Pourcentage de progression** du paiement  
✅ **Statut SOLDÉ** automatique quand le paiement est complet  
✅ **Suivi individuel** par élève et par motif  
✅ **Vue d'ensemble** de tous les paiements d'un élève  
✅ **Protection des données** : impossible de supprimer un motif avec paiements associés

---

## 🚀 PROCHAINES ÉTAPES

1. Exécuter le script SQL de migration : `migration_motifs_paiement.sql`
2. Redémarrer l'application Spring Boot
3. Tester les API avec Postman ou le frontend React
4. Créer les motifs de paiement nécessaires
5. Commencer à enregistrer les paiements des élèves

---

**Date de création:** 2025-10-31  
**Version:** 1.0.0
