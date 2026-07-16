package com.ong.compta.service;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.*;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.LignePaiementRepository;
import com.ong.compta.repository.ListePaiementRepository;
import com.ong.compta.service.exception.EtatInvalideException;
import com.ong.compta.service.exception.ListePaiementInvalideException;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ListePaiementService {

    private final ListePaiementRepository listePaiementRepository;
    private final LignePaiementRepository lignePaiementRepository;
    private final AideService aideService;
    private final EcritureService ecritureService;
    private final CompteService compteService;

    public ListePaiementService(ListePaiementRepository listePaiementRepository,
                                 LignePaiementRepository lignePaiementRepository,
                                 AideService aideService,
                                 EcritureService ecritureService,
                                 CompteService compteService) {
        this.listePaiementRepository = listePaiementRepository;
        this.lignePaiementRepository = lignePaiementRepository;
        this.aideService = aideService;
        this.ecritureService = ecritureService;
        this.compteService = compteService;
    }

    public List<ListePaiement> lister() {
        return listePaiementRepository.findAll();
    }

    public ListePaiement trouver(Long id) {
        return listePaiementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Liste de paiement introuvable : " + id));
    }

    @Transactional
    public ListePaiement generer(int annee, int mois, String utilisateur) {
        if (listePaiementRepository.existsByAnneeAndMois(annee, mois)) {
            throw new EtatInvalideException("Une liste de paiement existe deja pour " + mois + "/" + annee + ".");
        }

        List<Aide> aidesActives = aideService.listerActivesPour(annee, mois);
        if (aidesActives.isEmpty()) {
            throw new ListePaiementInvalideException("Aucune aide active pour " + mois + "/" + annee + ".");
        }

        ListePaiement liste = new ListePaiement(annee, mois, utilisateur);
        for (Aide aide : aidesActives) {
            liste.ajouterLigne(new LignePaiement(aide, aide.getMontantMensuel()));
        }

        return listePaiementRepository.save(liste);
    }

    @Transactional
    public ListePaiement valider(Long id, String utilisateur) {
        ListePaiement liste = trouver(id);
        if (liste.getStatut() != StatutListePaiement.GENEREE) {
            throw new EtatInvalideException("Seule une liste GENEREE peut etre validee.");
        }
        liste.setStatut(StatutListePaiement.VALIDEE);
        liste.setValideePar(utilisateur);
        return liste;
    }

    @Transactional
    public void marquerLigneVersee(Long ligneId, LocalDate dateVersement, String referenceDecharge) {
        LignePaiement ligne = trouverLigne(ligneId);
        verifierListeValidee(ligne.getListe());
        ligne.marquerVerse(dateVersement, referenceDecharge);
    }

    @Transactional
    public void marquerLigneNonVersee(Long ligneId, String motif) {
        LignePaiement ligne = trouverLigne(ligneId);
        verifierListeValidee(ligne.getListe());
        ligne.marquerNonVerse(motif);
    }

    /**
     * Cloture atomique : toutes les lignes doivent etre VERSE ou NON_VERSE.
     * Pour chaque ligne VERSE, on genere une ecriture comptable (brouillon + validation
     * immediate) dans le perimetre du financement de l'aide.
     * Si une seule ecriture echoue, toute la transaction est annulee.
     */
    @Transactional
    public ListePaiement cloturer(Long id, String utilisateur) {
        ListePaiement liste = trouver(id);
        if (liste.getStatut() != StatutListePaiement.VALIDEE) {
            throw new EtatInvalideException("Seule une liste VALIDEE peut etre cloturee.");
        }

        for (LignePaiement ligne : liste.getLignes()) {
            if (ligne.getStatut() == StatutLignePaiement.A_PAYER) {
                throw new ListePaiementInvalideException(
                        "Toutes les lignes doivent etre traitees (VERSE ou NON_VERSE) avant la cloture.");
            }
        }

        Compte compteAides = compteService.lister().stream()
                .filter(c -> c.getNumero().startsWith("651"))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Compte d'aides (651x) introuvable."));

        Compte compteCaisse = compteService.lister().stream()
                .filter(Compte::isEstTresorerie)
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun compte de tresorerie trouve."));

        for (LignePaiement ligne : liste.getLignes()) {
            if (ligne.getStatut() != StatutLignePaiement.VERSE) {
                continue;
            }

            Financement financement = ligne.getAide().getFinancement();
            PerimetreType perimetre = PerimetreType.PROJET;
            LocalDate dateOp = ligne.getDateVersement();

            List<LigneSaisie> lignesSaisie = List.of(
                    LigneSaisie.debit(compteAides, ligne.getMontant(), null, null),
                    LigneSaisie.credit(compteCaisse, ligne.getMontant(), null, null));

            String libelle = "Aide " + ligne.getAide().getEnfant().getNom()
                    + " - " + liste.getMois() + "/" + liste.getAnnee();

            Ecriture ecriture = ecritureService.creerBrouillon(perimetre, financement, dateOp,
                    libelle, "LP-" + liste.getId(), TypeOperation.DECAISSEMENT, utilisateur, lignesSaisie);
            ecriture = ecritureService.valider(ecriture.getId(), utilisateur);
            ligne.setEcriture(ecriture);
        }

        liste.setStatut(StatutListePaiement.CLOTUREE);
        liste.setClotureePar(utilisateur);
        return liste;
    }

    private LignePaiement trouverLigne(Long ligneId) {
        return lignePaiementRepository.findById(ligneId)
                .orElseThrow(() -> new RessourceIntrouvableException("Ligne de paiement introuvable : " + ligneId));
    }

    private void verifierListeValidee(ListePaiement liste) {
        if (liste.getStatut() != StatutListePaiement.VALIDEE) {
            throw new EtatInvalideException("Les lignes ne peuvent etre modifiees que sur une liste VALIDEE.");
        }
    }
}
