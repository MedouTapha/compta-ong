package com.ong.compta.service;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneEcriture;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutEcriture;
import com.ong.compta.domain.enums.TypeOperation;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.EcritureRepository;
import com.ong.compta.service.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur comptable : creation des ecritures en brouillon, validation (RG-1 a RG-9)
 * et contre-passation. Toute regle de gestion vit ici, jamais dans un controleur.
 */
@Service
public class EcritureService {

    private final EcritureRepository ecritureRepository;
    private final NumerotationService numerotationService;
    private final PeriodeClotureeService periodeClotureeService;
    private final SoldeService soldeService;
    private final AuditService auditService;

    public EcritureService(EcritureRepository ecritureRepository,
                            NumerotationService numerotationService,
                            PeriodeClotureeService periodeClotureeService,
                            SoldeService soldeService,
                            AuditService auditService) {
        this.ecritureRepository = ecritureRepository;
        this.numerotationService = numerotationService;
        this.periodeClotureeService = periodeClotureeService;
        this.soldeService = soldeService;
        this.auditService = auditService;
    }

    /**
     * Cree une ecriture en BROUILLON. Applique les regles structurelles qui ne dependent
     * pas de l'etat concurrent de la base (RG-1, RG-4, RG-5).
     */
    @Transactional
    public Ecriture creerBrouillon(PerimetreType perimetreType, Financement financement, LocalDate dateOperation,
                                    String libelle, String referencePiece, TypeOperation typeOperation,
                                    String creePar, List<LigneSaisie> lignesSaisies) {
        verifierPieceJustificative(referencePiece);
        verifierFinancementObligatoire(perimetreType, financement);
        verifierEquilibre(lignesSaisies);
        verifierDestinationsObligatoires(lignesSaisies);

        Ecriture ecriture = new Ecriture(perimetreType, financement, dateOperation, libelle,
                referencePiece.trim(), typeOperation, creePar);
        for (LigneSaisie saisie : lignesSaisies) {
            ecriture.ajouterLigne(new LigneEcriture(saisie.compte(), saisie.debit(), saisie.credit(),
                    saisie.destination(), saisie.ligneBudget()));
        }
        return ecritureRepository.save(ecriture);
    }

    /**
     * Valide une ecriture BROUILLON : controle periode/solde/coherence puis attribue
     * le numero definitif (RG-3) en tout dernier lieu, une fois toutes les autres
     * regles satisfaites, afin qu'un echec quelconque ne consomme jamais de numero.
     */
    @Transactional
    public Ecriture valider(Long ecritureId, String utilisateur) {
        Ecriture ecriture = ecritureRepository.findById(ecritureId)
                .orElseThrow(() -> new RessourceIntrouvableException("Ecriture introuvable : " + ecritureId));

        if (ecriture.getStatut() != StatutEcriture.BROUILLON) {
            throw new EtatInvalideException("Seule une ecriture BROUILLON peut etre validee.");
        }

        periodeClotureeService.verifierPeriodeOuverte(ecriture.getDateOperation());
        verifierCoherenceCompteDedie(ecriture);
        verifierSoldeDisponible(ecriture);

        int numero = numerotationService.prochainNumero(ecriture.getPerimetreType(), ecriture.getFinancement());
        ecriture.setNumero(numero);
        ecriture.setStatut(StatutEcriture.VALIDEE);
        ecriture.setValidePar(utilisateur);
        ecriture.setValideLe(LocalDateTime.now());

        auditService.enregistrer(utilisateur, "VALIDATION_ECRITURE", "Ecriture", ecriture.getId(),
                "numero=" + numero + ";perimetre=" + ecriture.getPerimetreType());

        return ecriture;
    }

    /**
     * RG-2 : intangibilite. La seule facon de corriger une ecriture VALIDEE est de la
     * contre-passer (creation d'une nouvelle ecriture inverse, validee immediatement).
     * Il n'existe volontairement aucune methode modifier()/supprimer() sur une ecriture VALIDEE.
     */
    @Transactional
    public Ecriture contrePasser(Long ecritureId, String utilisateur, LocalDate dateContrePassation) {
        Ecriture origine = ecritureRepository.findById(ecritureId)
                .orElseThrow(() -> new RessourceIntrouvableException("Ecriture introuvable : " + ecritureId));

        if (origine.getStatut() != StatutEcriture.VALIDEE) {
            throw new EtatInvalideException("Seule une ecriture VALIDEE peut etre contre-passee.");
        }

        List<LigneSaisie> lignesInversees = origine.getLignes().stream()
                .map(l -> new LigneSaisie(l.getCompte(), l.getCredit(), l.getDebit(), l.getDestination(), l.getLigneBudget()))
                .toList();

        Ecriture contrepassation = creerBrouillon(origine.getPerimetreType(), origine.getFinancement(),
                dateContrePassation, "Contre-passation ecriture n." + origine.getNumero() + " - " + origine.getLibelle(),
                origine.getReferencePiece(), TypeOperation.CONTREPASSATION, utilisateur, lignesInversees);
        contrepassation.setEcritureOrigine(origine);

        contrepassation = valider(contrepassation.getId(), utilisateur);

        origine.setStatut(StatutEcriture.CONTREPASSEE);

        auditService.enregistrer(utilisateur, "CONTREPASSATION", "Ecriture", origine.getId(),
                "contrepasseePar=" + contrepassation.getId());

        return contrepassation;
    }

    // ------------------------------------------------------------------
    // RG-1 : equilibre de l'ecriture
    // ------------------------------------------------------------------
    private void verifierEquilibre(List<LigneSaisie> lignes) {
        if (lignes == null || lignes.size() < 2) {
            throw new EcritureIncompleteException("Une ecriture doit comporter au moins deux lignes.");
        }
        for (LigneSaisie ligne : lignes) {
            if (ligne.debit().compareTo(BigDecimal.ZERO) < 0 || ligne.credit().compareTo(BigDecimal.ZERO) < 0) {
                throw new MontantInvalideException("Les montants de debit/credit doivent etre positifs ou nuls.");
            }
            if (ligne.debit().compareTo(BigDecimal.ZERO) > 0 && ligne.credit().compareTo(BigDecimal.ZERO) > 0) {
                throw new MontantInvalideException("Une ligne ne peut pas porter a la fois un debit et un credit.");
            }
        }
        BigDecimal totalDebit = lignes.stream().map(LigneSaisie::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lignes.stream().map(LigneSaisie::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new EcritureDesequilibreeException(
                    "Ecriture desequilibree : debit=" + totalDebit + " credit=" + totalCredit);
        }
    }

    // ------------------------------------------------------------------
    // RG-4 : piece justificative obligatoire
    // ------------------------------------------------------------------
    private void verifierPieceJustificative(String referencePiece) {
        if (referencePiece == null || referencePiece.trim().isEmpty()) {
            throw new PieceJustificativeManquanteException("La reference de piece justificative est obligatoire.");
        }
    }

    // ------------------------------------------------------------------
    // RG-5 : financement et destination obligatoires sur les charges/produits
    // ------------------------------------------------------------------
    private void verifierFinancementObligatoire(PerimetreType perimetreType, Financement financement) {
        if (perimetreType == PerimetreType.PROJET && financement == null) {
            throw new FinancementManquantException("Une ecriture de perimetre PROJET doit reference un financement.");
        }
    }

    private void verifierDestinationsObligatoires(List<LigneSaisie> lignes) {
        for (LigneSaisie ligne : lignes) {
            int classe = ligne.compte().getClasse();
            boolean estCharge = classe == 6;
            boolean estProduit = classe == 7;
            if ((estCharge || estProduit) && ligne.destination() == null) {
                throw new DestinationManquanteException(
                        "La destination est obligatoire sur les lignes de charges/produits (compte " + ligne.compte().getNumero() + ").");
            }
        }
    }

    // ------------------------------------------------------------------
    // RG-9 : coherence compte dedie / perimetre
    // ------------------------------------------------------------------
    private void verifierCoherenceCompteDedie(Ecriture ecriture) {
        for (LigneEcriture ligne : ecriture.getLignes()) {
            Compte compte = ligne.getCompte();
            if (!compte.isDedie()) {
                continue;
            }
            Financement projetDedie = compte.getProjetDedie();
            if (ecriture.getFinancement() == null) {
                throw new CompteDedieIncoherentException(
                        "Le compte " + compte.getNumero() + " est dedie au projet " + projetDedie.getCode()
                                + " ; une ecriture generale ne peut pas l'utiliser.");
            }
            if (!projetDedie.getId().equals(ecriture.getFinancement().getId())) {
                throw new CompteDedieIncoherentException(
                        "Le compte " + compte.getNumero() + " est dedie au projet " + projetDedie.getCode()
                                + " ; il ne peut pas etre utilise par le projet " + ecriture.getFinancement().getCode() + ".");
            }
        }
    }

    // ------------------------------------------------------------------
    // RG-8 : solde disponible par perimetre (comptes de tresorerie partages)
    // ------------------------------------------------------------------
    private void verifierSoldeDisponible(Ecriture ecriture) {
        Map<Compte, BigDecimal> deltaParCompte = new HashMap<>();
        for (LigneEcriture ligne : ecriture.getLignes()) {
            Compte compte = ligne.getCompte();
            if (!compte.isEstTresorerie() || compte.isDedie()) {
                continue;
            }
            BigDecimal delta = ligne.getDebit().subtract(ligne.getCredit());
            deltaParCompte.merge(compte, delta, BigDecimal::add);
        }

        for (Map.Entry<Compte, BigDecimal> entry : deltaParCompte.entrySet()) {
            Compte compte = entry.getKey();
            BigDecimal soldeActuel = soldeService.soldeDisponible(compte, ecriture.getPerimetreType(), ecriture.getFinancement());
            BigDecimal soldeProjete = soldeActuel.add(entry.getValue());
            if (soldeProjete.compareTo(BigDecimal.ZERO) < 0) {
                throw new SoldeInsuffisantException(
                        "Solde disponible insuffisant sur le compte " + compte.getNumero()
                                + " pour ce perimetre (solde apres operation : " + soldeProjete + ").");
            }
        }
    }
}
